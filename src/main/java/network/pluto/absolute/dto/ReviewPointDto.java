package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.bibliotheca.models.ReviewPoint;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class ReviewPointDto {

    @ApiModelProperty(readOnly = true)
    private double total;

    @ApiModelProperty(required = true)
    @NotNull
    private int originality;

    @ApiModelProperty(required = true)
    @NotNull
    private int significance;

    @ApiModelProperty(required = true)
    @NotNull
    private int validity;

    @ApiModelProperty(required = true)
    @NotNull
    private int organization;

    private String originalityComment;
    private String significanceComment;
    private String validityComment;
    private String organizationComment;

    private String review;

    public ReviewPointDto(ReviewPoint point) {
        this.total = point.getTotal();

        this.originality = point.getOriginality();
        this.significance = point.getSignificance();
        this.validity = point.getValidity();
        this.organization = point.getOrganization();

        this.review = point.getReview();
    }

    public ReviewPoint toEntity() {
        ReviewPoint point = new ReviewPoint();

        point.setOriginality(this.originality);
        point.setSignificance(this.significance);
        point.setValidity(this.validity);
        point.setOrganization(this.organization);

        point.updateTotal();

        point.setReview(this.review);

        return point;
    }
}
