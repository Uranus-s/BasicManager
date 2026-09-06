package com.basic.sericve.ai.assistant.capability.dictitem;

import com.basic.api.dto.sysDictItem.*;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDict.entity.SysDict;
import com.basic.dao.sysDictItem.entity.SysDictItem;
import com.basic.sericve.ai.assistant.action.model.ActionPreviewField;
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
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.*;

/** 字典项查询与需确认的单项写操作能力。 */
@Component
@RequiredArgsConstructor
public class DictItemAssistantCapability implements AssistantCapability {
    private static final String QUERY = "system:dict:query";
    private static final int MAX = 20;
    private final ISysDictItemService itemService;
    private final ISysDictService dictService;
    @Override public String capabilityId() { return "dict_item"; }
    @Override public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> ops = new ArrayList<>();
        ops.add(query("dict_item_search", SearchInput.class, SearchResult.class).description("查询字典项").permissions(QUERY).execute(this::search).build());
        ops.add(query("dict_item_get", IdInput.class, ItemResult.class).description("读取字典项详情").permissions(QUERY).execute(i -> toResult(itemService.getDictItemById(i.id()))).build());
        ops.add(action("dict_item_create_propose", CreateInput.class, DictItemSnapshot.class).actionType("DICT_ITEM_CREATE").description("新增字典项待确认").permissions("system:dict:item:add").prepare(this::prepareCreate).preview("新增字典项待确认", DictItemAssistantCapability::preview).validate(this::validate).execute(this::create).build());
        ops.add(action("dict_item_update_propose", UpdateInput.class, DictItemSnapshot.class).actionType("DICT_ITEM_UPDATE").description("修改字典项待确认").permissions("system:dict:item:edit").prepare(this::prepareUpdate).preview("修改字典项待确认", DictItemAssistantCapability::preview).validate(this::validate).execute(this::update).build());
        ops.add(action("dict_item_delete_propose", IdInput.class, DictItemSnapshot.class).actionType("DICT_ITEM_DELETE").description("删除字典项待确认").permissions("system:dict:item:delete").prepare(this::prepareDelete).preview("删除字典项待确认", DictItemAssistantCapability::preview).validate(this::validate).execute(this::delete).build());
        ops.add(query("dict_item_values", CodeInput.class, SearchResult.class).description("按字典编码查询启用字典项").permissions(QUERY).execute(this::byCode).build());
        return List.copyOf(ops);
    }
    private SearchResult search(SearchInput in) { DictItemQueryDTO q=new DictItemQueryDTO(); q.setDictId(in.dictId()); q.setItemValue(text(in.itemValue())); q.setItemLabel(text(in.itemLabel())); q.setStatus(in.status()); q.setPageNum(1); q.setPageSize(MAX); PageResult<DictItemVO> p=itemService.getDictItemList(q); return new SearchResult(p.getTotal(), p.getList()==null?List.of():p.getList().stream().limit(MAX).map(this::toResult).toList()); }
    private SearchResult byCode(CodeInput in) { List<DictItemVO> list=itemService.getDictItemsByDictCode(text(in.dictCode())); return new SearchResult((long)list.size(), list.stream().limit(MAX).map(this::toResult).toList()); }
    private DictItemSnapshot prepareCreate(CreateInput i) { SysDict d=dict(i.dictId()); return new DictItemSnapshot(null,d.getId(),d.getDictCode(),d.getDictName(),i.itemValue(),i.itemLabel(),i.sort(),i.status()==null?(byte)1:i.status(),null,d.getVersion()); }
    private DictItemSnapshot prepareUpdate(UpdateInput i) { SysDictItem x=item(i.id()); SysDict d=dict(x.getDictId()); return new DictItemSnapshot(x.getId(),d.getId(),d.getDictCode(),d.getDictName(),i.itemValue()==null?x.getItemValue():i.itemValue(),i.itemLabel()==null?x.getItemLabel():i.itemLabel(),i.sort()==null?x.getSort():i.sort(),i.status()==null?x.getStatus():i.status(),x.getVersion(),d.getVersion()); }
    private DictItemSnapshot prepareDelete(IdInput i) { SysDictItem x=item(i.id()); SysDict d=dict(x.getDictId()); return new DictItemSnapshot(x.getId(),d.getId(),d.getDictCode(),d.getDictName(),x.getItemValue(),x.getItemLabel(),x.getSort(),x.getStatus(),x.getVersion(),d.getVersion()); }
    private void validate(DictItemSnapshot p) { if (p.id()!=null) { SysDictItem current=itemService.getById(p.id()); if (current==null) throw new BusinessException(ResultEnum.DATA_NOT_EXIST); if (p.version()!=null && !p.version().equals(current.getVersion())) throw new BusinessException(ResultEnum.DATA_VERSION_EXPIRED); } dict(p.dictId()); }
    private ActionPreviewField field(String n,Object v){return new ActionPreviewField(n,v==null?"":String.valueOf(v));}
    private static List<ActionPreviewField> preview(DictItemSnapshot p){return List.of(new ActionPreviewField("字典",p.dictCode()),new ActionPreviewField("值",p.itemValue()),new ActionPreviewField("标签",p.itemLabel()));}
    private com.basic.sericve.ai.assistant.action.model.ActionExecutionResult create(DictItemSnapshot p){DictItemAddDTO d=new DictItemAddDTO();d.setDictId(p.dictId());d.setItemValue(p.itemValue());d.setItemLabel(p.itemLabel());d.setSort(p.sort());d.setStatus(p.status());Long id=itemService.addDictItem(d);return result(id,"字典项新增成功，字典项ID："+id);}
    private com.basic.sericve.ai.assistant.action.model.ActionExecutionResult update(DictItemSnapshot p){DictItemUpdateDTO d=new DictItemUpdateDTO();d.setId(p.id());d.setItemValue(p.itemValue());d.setItemLabel(p.itemLabel());d.setSort(p.sort());d.setStatus(p.status());itemService.updateDictItem(d,p.version());return result(p.id(),"字典项修改成功");}
    private com.basic.sericve.ai.assistant.action.model.ActionExecutionResult delete(DictItemSnapshot p){itemService.deleteDictItem(p.id(),p.version());return result(p.id(),"字典项删除成功");}
    private SysDict dict(Long id){SysDict d=dictService.getById(id);if(d==null)throw new BusinessException(ResultEnum.RELATION_DATA_NOT_EXIST);return d;}
    private SysDictItem item(Long id){SysDictItem x=itemService.getById(id);if(x==null)throw new BusinessException(ResultEnum.DATA_NOT_EXIST);return x;}
    private ItemResult toResult(DictItemVO x){return new ItemResult(x.getId(),x.getDictId(),x.getItemValue(),x.getItemLabel(),x.getSort(),x.getStatus(),x.getCreateTime(),x.getUpdateTime());}
    private static String text(String x){return StringUtils.hasText(x)?x.trim():null;}
    public record SearchInput(Long dictId,String itemValue,String itemLabel,Byte status){}
    public record CodeInput(@NotNull String dictCode){}
    public record IdInput(@NotNull Long id){}
    public record CreateInput(@NotNull Long dictId,String itemValue,String itemLabel,Integer sort,Byte status){}
    public record UpdateInput(@NotNull Long id,String itemValue,String itemLabel,Integer sort,Byte status){}
    public record SearchResult(Long total,List<ItemResult> items){}
    public record ItemResult(Long id,Long dictId,String itemValue,String itemLabel,Integer sort,Byte status,LocalDateTime createTime,LocalDateTime updateTime){}
    public record DictItemSnapshot(Long id,Long dictId,String dictCode,String dictName,String itemValue,String itemLabel,Integer sort,Byte status,Integer version,Integer dictVersion){}
}
