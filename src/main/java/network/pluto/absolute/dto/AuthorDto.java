package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.enums.AuthorType;
import network.pluto.bibliotheca.models.Author;
import network.pluto.bibliotheca.models.Member;

@Data
public class AuthorDto {
    private Long id;
    private MemberDto member;
    private String type;
    private String name;
    private String organization;

    public AuthorDto() {

    }

    public AuthorDto(Author author) {
        this.id = author.getAuthorId();
        Member member = author.getMember();
        if (member != null) {
            this.member = MemberDto.fromEntity(member);
        }

        this.type = author.getType().name();
        this.name = author.getName();
        this.organization = author.getOrganization();
    }

    public Author toEntity() {
        Author author = new Author();
        if(this.member != null) {
            author.setMember(this.member.toEntity());
        }
        author.setType(AuthorType.valueOf(this.type));
        author.setName(this.name);
        author.setOrganization(this.organization);

        return author;
    }
}
