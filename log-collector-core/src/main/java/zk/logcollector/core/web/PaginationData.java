package zk.logcollector.core.web;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class PaginationData<T> {

    private int pageNum;

    private int pageSize;

    private int totalCount;

    private List<T> data;

}
