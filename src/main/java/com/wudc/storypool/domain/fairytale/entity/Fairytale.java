package com.wudc.storypool.domain.fairytale.entity;

import com.wudc.storypool.common.base.BaseEntity;
import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Fairytale extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String storyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer pageNumber = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FairytaleStatus status = FairytaleStatus.INIT;

    @Column(length = 255)
    private String message = "";

    @OneToMany(mappedBy = "fairytale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("pageIndex ASC")
    private List<FairytaleePage> pageList = new ArrayList<>();

    public static Fairytale create(String userId, String storyId, String name) {
        Fairytale fairytale = new Fairytale();
        fairytale.userId = userId;
        fairytale.storyId = storyId;
        fairytale.name = name;
        fairytale.pageNumber = 0;
        fairytale.status = FairytaleStatus.INIT;
        fairytale.message = "";
        return fairytale;
    }

    public void updateStatus(FairytaleStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void completeGeneration(List<FairytaleePage> pages) {
        this.pageList.clear();
        this.pageList.addAll(pages);
        this.pageNumber = pages.size();
        this.status = FairytaleStatus.COMPLETED;
        this.message = "동화 생성이 완료되었습니다.";
        
        // Set fairytale reference for each page
        pages.forEach(page -> page.setFairytale(this));
    }

    public FairytaleePage getThumbnailPage() {
        return pageList.isEmpty() ? null : pageList.get(0);
    }
}