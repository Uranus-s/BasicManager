package com.basic.sericve.sysNotice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basic.api.dto.sysNotice.NoticeAddDTO;
import com.basic.api.dto.sysNotice.NoticeQueryDTO;
import com.basic.api.dto.sysNotice.NoticeUpdateDTO;
import com.basic.api.dto.sysNotice.NoticeVisibleQueryDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.api.vo.sysNotice.NoticeDetailVO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.api.vo.sysNotice.NoticeTargetOptionsVO;
import com.basic.api.vo.sysNotice.NoticeVO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDept.entity.SysDept;
import com.basic.dao.sysNotice.entity.SysNotice;
import com.basic.dao.sysNotice.entity.SysNoticeTarget;
import com.basic.dao.sysNotice.mapper.SysNoticeMapper;
import com.basic.dao.sysNotice.mapper.SysNoticeTargetMapper;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.basic.sericve.sysDictItem.service.ISysDictItemService;
import com.basic.sericve.sysNotice.model.NoticeVisibilityContext;
import com.basic.sericve.sysNotice.service.ISysNoticeService;
import com.basic.sericve.sysNotice.support.NoticeVisibilityContextResolver;
import com.basic.sericve.sysRole.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 通知公告业务实现，负责生命周期、接收目标和用户可见性边界。
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements ISysNoticeService {

    private static final String NOTICE_TYPE_DICT = "sys_notice_type";
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_TARGETED = "TARGETED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_DEPT = "DEPT";
    private static final int DEFAULT_LATEST_LIMIT = 5;
    private static final int MAX_LATEST_LIMIT = 10;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeTargetMapper targetMapper;
    private final ISysDictItemService dictItemService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final NoticeVisibilityContextResolver visibilityContextResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addNotice(NoticeAddDTO dto) {
        validateNoticeType(dto.getNoticeType());
        List<Long> roleIds = normalizeIds(dto.getRoleIds());
        List<Long> deptIds = normalizeIds(dto.getDeptIds());
        validateTargets(dto.getScopeType(), roleIds, deptIds);

        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle().trim());
        notice.setNoticeType(dto.getNoticeType());
        notice.setContent(dto.getContent());
        notice.setScopeType(dto.getScopeType());
        notice.setStatus(STATUS_DRAFT);
        noticeMapper.insert(notice);
        replaceTargets(notice.getId(), dto.getScopeType(), roleIds, deptIds);
        return notice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeUpdateDTO dto) {
        requireNotice(dto.getId());
        validateNoticeType(dto.getNoticeType());
        List<Long> roleIds = normalizeIds(dto.getRoleIds());
        List<Long> deptIds = normalizeIds(dto.getDeptIds());
        validateTargets(dto.getScopeType(), roleIds, deptIds);

        // 仅提交可编辑字段，避免用客户端数据覆盖当前状态和发布时间。
        SysNotice notice = new SysNotice();
        notice.setId(dto.getId());
        notice.setTitle(dto.getTitle().trim());
        notice.setNoticeType(dto.getNoticeType());
        notice.setContent(dto.getContent());
        notice.setScopeType(dto.getScopeType());
        notice.setVersion(dto.getVersion());
        updateWithVersionCheck(notice);
        replaceTargets(dto.getId(), dto.getScopeType(), roleIds, deptIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        SysNotice notice = requireNotice(id);
        if (!Set.of(STATUS_DRAFT, STATUS_WITHDRAWN).contains(notice.getStatus())) {
            throw new BusinessException(ResultEnum.NOTICE_STATUS_INVALID);
        }
        if (noticeMapper.deleteDraftOrWithdrawnByIdAndVersion(id, notice.getVersion()) == 0) {
            throw new BusinessException(ResultEnum.DATA_VERSION_EXPIRED);
        }
        targetMapper.deleteByNoticeId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(Long id) {
        SysNotice notice = requireNotice(id);
        if (!Set.of(STATUS_DRAFT, STATUS_WITHDRAWN).contains(notice.getStatus())) {
            throw new BusinessException(ResultEnum.NOTICE_STATUS_INVALID);
        }
        notice.setStatus(STATUS_PUBLISHED);
        notice.setPublishTime(LocalDateTime.now());
        updateWithVersionCheck(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawNotice(Long id) {
        SysNotice notice = requireNotice(id);
        if (!STATUS_PUBLISHED.equals(notice.getStatus())) {
            throw new BusinessException(ResultEnum.NOTICE_STATUS_INVALID);
        }
        notice.setStatus(STATUS_WITHDRAWN);
        updateWithVersionCheck(notice);
    }

    @Override
    public NoticeVO getNoticeById(Long id) {
        SysNotice notice = requireNotice(id);
        NoticeVO vo = toNoticeVO(notice);
        List<SysNoticeTarget> targets = targetMapper.selectByNoticeId(id);
        vo.setRoleIds(targetIds(targets, TARGET_ROLE));
        vo.setDeptIds(targetIds(targets, TARGET_DEPT));
        return vo;
    }

    @Override
    public PageResult<NoticeListVO> getNoticeList(NoticeQueryDTO dto) {
        validateStatusFilter(dto.getStatus());
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like(SysNotice::getTitle, dto.getTitle().trim());
        }
        if (StringUtils.hasText(dto.getNoticeType())) {
            wrapper.eq(SysNotice::getNoticeType, dto.getNoticeType());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(SysNotice::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysNotice::getUpdateTime).orderByDesc(SysNotice::getId);

        Page<SysNotice> page = noticeMapper.selectPage(
                new Page<>(pageNum(dto.getPageNum()), pageSize(dto.getPageSize())), wrapper);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(this::toListVO).toList());
    }

    @Override
    public NoticeTargetOptionsVO getTargetOptions() {
        NoticeTargetOptionsVO result = new NoticeTargetOptionsVO();
        result.setRoles(roleService.getAllRoles().stream()
                .map(this::toRoleOption)
                .toList());
        result.setDepartments(deptService.getDeptTree());
        return result;
    }

    @Override
    public List<NoticeListVO> getLatestVisibleNotices(Long userId, Integer limit) {
        NoticeVisibilityContext context = visibilityContextResolver.resolve(userId);
        int safeLimit = limit == null ? DEFAULT_LATEST_LIMIT : Math.max(1, Math.min(limit, MAX_LATEST_LIMIT));
        return noticeMapper.selectLatestVisible(context.roleIds(), context.deptIds(), safeLimit).stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public PageResult<NoticeListVO> getVisibleNoticeList(Long userId, NoticeVisibleQueryDTO dto) {
        NoticeVisibilityContext context = visibilityContextResolver.resolve(userId);
        Page<SysNotice> page = noticeMapper.selectVisiblePage(
                new Page<>(pageNum(dto.getPageNum()), pageSize(dto.getPageSize())),
                trimToNull(dto.getTitle()),
                trimToNull(dto.getNoticeType()),
                context.roleIds(),
                context.deptIds());
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(this::toListVO).toList());
    }

    @Override
    public NoticeDetailVO getVisibleNoticeById(Long userId, Long id) {
        NoticeVisibilityContext context = visibilityContextResolver.resolve(userId);
        SysNotice notice = noticeMapper.selectVisibleById(id, context.roleIds(), context.deptIds());
        if (notice == null) {
            throw new BusinessException(ResultEnum.NOTICE_NOT_FOUND_OR_INACTIVE);
        }
        NoticeDetailVO vo = new NoticeDetailVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    private void validateNoticeType(String noticeType) {
        boolean valid = dictItemService.getDictItemsByDictCode(NOTICE_TYPE_DICT).stream()
                .anyMatch(item -> item.getStatus() != null
                        && item.getStatus() == 1
                        && Objects.equals(noticeType, item.getItemValue()));
        if (!valid) {
            throw new BusinessException(ResultEnum.NOTICE_TYPE_INVALID);
        }
    }

    private void validateTargets(String scopeType, List<Long> roleIds, List<Long> deptIds) {
        if (SCOPE_ALL.equals(scopeType)) {
            return;
        }
        if (!SCOPE_TARGETED.equals(scopeType)) {
            throw new BusinessException(ResultEnum.PARAM_INVALID);
        }
        if (roleIds.isEmpty() && deptIds.isEmpty()) {
            throw new BusinessException(ResultEnum.NOTICE_TARGET_REQUIRED);
        }

        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleService.listByIds(roleIds);
            boolean valid = roles.size() == roleIds.size()
                    && roles.stream().allMatch(role -> role.getStatus() != null && role.getStatus() == 1);
            if (!valid) {
                throw new BusinessException(ResultEnum.NOTICE_TARGET_INVALID);
            }
        }
        if (!deptIds.isEmpty() && deptService.listByIds(deptIds).size() != deptIds.size()) {
            throw new BusinessException(ResultEnum.NOTICE_TARGET_INVALID);
        }
    }

    private void replaceTargets(Long noticeId, String scopeType, List<Long> roleIds, List<Long> deptIds) {
        targetMapper.deleteByNoticeId(noticeId);
        if (SCOPE_ALL.equals(scopeType)) {
            return;
        }

        List<SysNoticeTarget> targets = new ArrayList<>(roleIds.size() + deptIds.size());
        roleIds.forEach(roleId -> targets.add(target(noticeId, TARGET_ROLE, roleId)));
        deptIds.forEach(deptId -> targets.add(target(noticeId, TARGET_DEPT, deptId)));
        targetMapper.insertBatch(targets);
    }

    private SysNoticeTarget target(Long noticeId, String targetType, Long targetId) {
        SysNoticeTarget target = new SysNoticeTarget();
        target.setNoticeId(noticeId);
        target.setTargetType(targetType);
        target.setTargetId(targetId);
        return target;
    }

    private SysNotice requireNotice(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultEnum.NOTICE_NOT_FOUND_OR_INACTIVE);
        }
        return notice;
    }

    private void updateWithVersionCheck(SysNotice notice) {
        if (noticeMapper.updateById(notice) == 0) {
            throw new BusinessException(ResultEnum.DATA_VERSION_EXPIRED);
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        ids.stream().filter(java.util.Objects::nonNull).forEach(uniqueIds::add);
        return List.copyOf(uniqueIds);
    }

    private List<Long> targetIds(List<SysNoticeTarget> targets, String targetType) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        return targets.stream()
                .filter(target -> targetType.equals(target.getTargetType()))
                .map(SysNoticeTarget::getTargetId)
                .toList();
    }

    private NoticeVO toNoticeVO(SysNotice notice) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    private NoticeListVO toListVO(SysNotice notice) {
        NoticeListVO vo = new NoticeListVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    private NoticeTargetOptionsVO.RoleOption toRoleOption(RoleListVO role) {
        NoticeTargetOptionsVO.RoleOption option = new NoticeTargetOptionsVO.RoleOption();
        option.setId(role.getId());
        option.setRoleCode(role.getRoleCode());
        option.setRoleName(role.getRoleName());
        return option;
    }

    private void validateStatusFilter(String status) {
        if (StringUtils.hasText(status)
                && !Set.of(STATUS_DRAFT, STATUS_PUBLISHED, STATUS_WITHDRAWN).contains(status)) {
            throw new BusinessException(ResultEnum.NOTICE_STATUS_INVALID);
        }
    }

    private long pageNum(Integer value) {
        return value == null ? 1L : Math.max(1, value);
    }

    private long pageSize(Integer value) {
        return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
