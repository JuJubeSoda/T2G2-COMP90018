package org.unimelb.survey.vo;

import org.unimelb.common.vo.MyPage;
import lombok.Data;

@Data
public class SurveyQuery extends MyPage {
    private String title;
    private Integer status;
    private String orderCondition;

    private Integer star;

    public boolean isAsc() {
        if (orderCondition != null && orderCondition.indexOf("desc") > -1) {
            return false;
        }
        return true;
    }

    public String getOrderColumn() {
        if (orderCondition == null) {
            orderCondition = "";
        }
        return orderCondition.replaceAll("desc", "").trim();
    }
}
