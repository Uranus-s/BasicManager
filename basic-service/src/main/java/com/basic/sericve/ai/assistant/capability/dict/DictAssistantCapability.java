package com.basic.sericve.ai.assistant.capability.dict;

import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.result.PageResult;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDict.entity.SysDict;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysDict.service.ISysDictService;
import com.basic.sericve.sysDictItem.service.ISysDictItemService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.action;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.result;

/** 字典只读 AI 能力，返回字典及字典项的安全摘要，不包含任何写入操作。 */
@Component
@RequiredArgsConstructor
public class DictAssistantCapability implements AssistantCapability {

    private static final String DICT_QUERY = "system:dict:query";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int MAX_ITEMS = 20;

    private final ISysDictService dictService;
    private final ISysDictItemService dictItemService;

    @Override
    public String capabilityId() {
        return "dict";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("dict_search", DictSearchInput.class, DictSearchResult.class)
                .description("按字典编码、名称或状态查询字典，分页默认10条且最多20条")
                .permissions(DICT_QUERY)
                .execute(this::search)
                .build());
        operations.add(query("dict_get", DictIdInput.class, DictDetailResult.class)
                .description("按ID读取字典详情")
                .permissions(DICT_QUERY)
                .execute(input -> toDetail(dictService.getDictById(input.id())))
                .build());
        operations.add(query("dict_items", DictItemsInput.class, DictItemsResult.class)
                .description("按字典编码读取启用的字典项，最多返回20条")
                .permissions(DICT_QUERY)
                .execute(this::items)
                .build());
        operations.add(action("dict_create_propose", DictCreateInput.class, DictSnapshot.class)
                .actionType("DICT_CREATE").description("生成新增字典预览并等待用户确认；不会直接新增")
                .permissions("system:dict:add").prepare(this::prepareCreate)
                .preview("新增字典待确认", payload -> fields("字典编码", payload.dictCode(), "字典名称", payload.dictName(), "状态", payload.status()))
                .validate(this::validateCreate).execute(payload -> {
                    DictAddDTO dto = new DictAddDTO(); dto.setDictCode(payload.dictCode()); dto.setDictName(payload.dictName()); dto.setStatus(payload.status());
                    Long id = dictService.addDict(dto); return result(id, "字典新增成功，字典ID：" + id);
                }).build());
        operations.add(action("dict_update_propose", DictUpdateInput.class, DictSnapshot.class)
                .actionType("DICT_UPDATE").description("生成修改字典预览并等待用户确认；不会直接修改")
                .permissions("system:dict:edit").prepare(this::prepareUpdate)
                .preview("修改字典待确认", payload -> fields("字典ID", payload.id(), "字典编码", payload.dictCode(), "字典名称", payload.dictName(), "状态", payload.status()))
                .validate(this::validateSnapshot).execute(payload -> {
                    DictUpdateDTO dto = new DictUpdateDTO(); dto.setId(payload.id()); dto.setDictName(payload.dictName()); dto.setStatus(payload.status());
                    dictService.updateDict(dto, payload.version()); return result(payload.id(), "字典修改成功，字典ID：" + payload.id());
                }).build());
        operations.add(action("dict_delete_propose", DictDeleteInput.class, DictSnapshot.class)
                .actionType("DICT_DELETE").description("生成删除字典预览并等待用户确认；不会直接删除")
                .permissions("system:dict:delete").prepare(this::prepareDelete)
                .preview("删除字典待确认", payload -> fields("字典ID", payload.id(), "字典编码", payload.dictCode(), "字典名称", payload.dictName()))
                .validate(this::validateSnapshot).execute(payload -> {
                    dictService.deleteDict(payload.id(), payload.version()); return result(payload.id(), "字典删除成功，字典ID：" + payload.id());
                }).build());
        return List.copyOf(operations);
    }

    private DictSnapshot prepareCreate(DictCreateInput input) { return new DictSnapshot(null, trimToNull(input.dictCode()), trimToNull(input.dictName()), input.status() == null ? (byte) 1 : input.status(), null); }
    private DictSnapshot prepareUpdate(DictUpdateInput input) { SysDict d = dictService.getById(input.id()); if (d == null) throw new BusinessException(ResultEnum.DATA_NOT_EXIST); return new DictSnapshot(d.getId(), d.getDictCode(), trimToNull(input.dictName()), input.status() == null ? d.getStatus() : input.status(), d.getVersion()); }
    private DictSnapshot prepareDelete(DictDeleteInput input) { SysDict d = dictService.getById(input.id()); if (d == null) throw new BusinessException(ResultEnum.DATA_NOT_EXIST); return new DictSnapshot(d.getId(), d.getDictCode(), d.getDictName(), d.getStatus(), d.getVersion()); }
    private void validateCreate(DictSnapshot payload) { DictAddDTO dto = new DictAddDTO(); dto.setDictCode(payload.dictCode()); dto.setDictName(payload.dictName()); dto.setStatus(payload.status()); if (dictService.getByDictCode(payload.dictCode()) != null) throw new BusinessException(ResultEnum.DATA_ALREADY_EXIST); }
    private void validateSnapshot(DictSnapshot payload) { SysDict current = dictService.getById(payload.id()); if (current == null) throw new BusinessException(ResultEnum.DATA_NOT_EXIST); if (payload.version() != null && !payload.version().equals(current.getVersion())) throw new BusinessException(ResultEnum.DATA_VERSION_EXPIRED); }
    private static List<com.basic.sericve.ai.assistant.action.model.ActionPreviewField> fields(Object... values) { List<com.basic.sericve.ai.assistant.action.model.ActionPreviewField> out = new ArrayList<>(); for (int i = 0; i < values.length; i += 2) out.add(new com.basic.sericve.ai.assistant.action.model.ActionPreviewField(String.valueOf(values[i]), values[i + 1] == null ? "" : String.valueOf(values[i + 1]))); return List.copyOf(out); }

    private DictSearchResult search(DictSearchInput input) {
        DictQueryDTO query = new DictQueryDTO();
        query.setDictCode(trimToNull(input.dictCode()));
        query.setDictName(trimToNull(input.dictName()));
        query.setStatus(input.status());
        query.setPageNum(pageNum(input.pageNum()));
        query.setPageSize(pageSize(input.pageSize()));
        PageResult<DictVO> page = dictService.getDictList(query);
        List<DictSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_PAGE_SIZE).map(DictAssistantCapability::toSummary).toList();
        return new DictSearchResult(page.getTotal(), items);
    }

    private DictItemsResult items(DictItemsInput input) {
        List<DictItemVO> source = dictItemService.getDictItemsByDictCode(trimToNull(input.dictCode()));
        int total = source == null ? 0 : source.size();
        List<DictItemSummary> items = source == null ? List.of() : source.stream()
                .limit(MAX_ITEMS).map(item -> new DictItemSummary(item.getId(), item.getDictId(),
                        item.getItemValue(), item.getItemLabel(), item.getSort(), item.getStatus()))
                .toList();
        return new DictItemsResult(trimToNull(input.dictCode()), (long) total, items, total > MAX_ITEMS);
    }

    private static DictSummary toSummary(DictVO dict) {
        return new DictSummary(dict.getId(), dict.getDictCode(), dict.getDictName(), dict.getStatus(),
                dict.getCreateTime(), dict.getUpdateTime());
    }

    private static DictDetailResult toDetail(DictVO dict) {
        return new DictDetailResult(dict.getId(), dict.getDictCode(), dict.getDictName(), dict.getStatus(),
                dict.getCreateTime(), dict.getUpdateTime());
    }

    private static int pageNum(Integer value) {
        return value == null || value < 1 ? 1 : value;
    }

    private static int pageSize(Integer value) {
        return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE));
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public record DictSearchInput(String dictCode, String dictName, Byte status, Integer pageNum, Integer pageSize) {
    }

    public record DictIdInput(@NotNull(message = "字典ID不能为空") Long id) {
    }

    public record DictItemsInput(@NotNull(message = "字典编码不能为空") String dictCode) {
    }

    public record DictSearchResult(Long total, List<DictSummary> items) {
    }

    public record DictSummary(Long id, String dictCode, String dictName, Byte status,
                              LocalDateTime createTime, LocalDateTime updateTime) {
    }

    public record DictDetailResult(Long id, String dictCode, String dictName, Byte status,
                                   LocalDateTime createTime, LocalDateTime updateTime) {
    }

    public record DictItemsResult(String dictCode, Long total, List<DictItemSummary> items, boolean truncated) {
    }

    public record DictItemSummary(Long id, Long dictId, String itemValue, String itemLabel,
                                  Integer sort, Byte status) {
    }
    public record DictCreateInput(String dictCode, String dictName, Byte status) { }
    public record DictUpdateInput(@NotNull Long id, String dictName, Byte status) { }
    public record DictDeleteInput(@NotNull Long id) { }
    public record DictSnapshot(Long id, String dictCode, String dictName, Byte status, Integer version) { }
}
