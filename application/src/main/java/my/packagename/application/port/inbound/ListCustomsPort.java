package my.packagename.application.port.inbound;

import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;

@FunctionalInterface
public interface ListCustomsPort {
  PageResult<Custom> execute(PageQuery query);
}
