package io.scinapse.api.model.profile;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Profile extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String affiliation;

    @Column
    private String email;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private Member member;

//    @OneToMany(mappedBy = "profile")
//    private List<ProfileAuthor> authors = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<ProfileEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<ProfileExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<ProfileAward> awards = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<ProfileSelectedPublication> selectedPublications = new ArrayList<>();

}
