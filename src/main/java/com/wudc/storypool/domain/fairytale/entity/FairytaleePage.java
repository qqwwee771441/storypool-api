package com.wudc.storypool.domain.fairytale.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fairytale_page")
public class FairytaleePage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fairytale_id", nullable = false)
    private Fairytale fairytale;

    @Column(nullable = false)
    private Integer pageIndex;

    @Column(nullable = false, length = 50)
    private String mood;

    @Column(nullable = false, length = 2000)
    private String story;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    public static FairytaleePage create(Fairytale fairytale, Integer pageIndex, String mood, String story, String imageUrl) {
        FairytaleePage page = new FairytaleePage();
        page.fairytale = fairytale;
        page.pageIndex = pageIndex;
        page.mood = mood;
        page.story = story;
        page.imageUrl = imageUrl;
        return page;
    }
}