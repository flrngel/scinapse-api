package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.enums.AuthorType;
import network.pluto.bibliotheca.models.Author;

@NoArgsConstructor
@Data
public class AuthorDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    private MemberDto member;

    @ApiModelProperty(required = true)
    private AuthorType type;

    @ApiModelProperty(required = true)
    private String name;
    private String organization;

    public AuthorDto(Author author) {
        this.id = author.getAuthorId();

        if (author.getMember() != null) {
            this.member = new MemberDto(author.getMember());
        }

        this.type = author.getType();
        this.name = author.getName();
        this.organization = author.getOrganization();
    }

    public Author toEntity() {
        Author author = new Author();
        if (this.member != null) {
            author.setMember(this.member.toEntity());
        }
        author.setType(this.type);
        author.setName(this.name);
        author.setOrganization(this.organization);

        return author;
    }
}
