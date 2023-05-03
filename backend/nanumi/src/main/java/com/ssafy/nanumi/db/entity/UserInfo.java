package com.ssafy.nanumi.db.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="user_info")
public class UserInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ColumnDefault("0")
    @Column(name = "star_total", columnDefinition = "INT", nullable = false)
    private int starTotal;

    @ColumnDefault("0")
    @Column(name = "star_count", columnDefinition = "INT", nullable = false)
    private int starCount;

    @ColumnDefault("0")
    @Column(name = "rating_total", columnDefinition = "INT", nullable = false)
    private int ratingTotal;

    @ColumnDefault("0")
    @Column(name = "rating_count", columnDefinition = "INT", nullable = false)
    private int ratingCount;

    @ColumnDefault("'브론즈'")
    @Column(name = "tier", columnDefinition = "VARCHAR(20)")
    private String tier;

    @ColumnDefault("0")
    @Column(name = "temperature", columnDefinition = "DOUBLE", nullable = false)
    private double temperature;

    @ColumnDefault("0")
    @Column(name = "visit_count", columnDefinition = "BIGINT", nullable = false)
    private long visitCount;

    @ColumnDefault("0")
    @Column(name = "give_count", columnDefinition = "INT", nullable = false)
    private int giveCount;

    @ColumnDefault("0")
    @Column(name = "given_count", columnDefinition = "INT", nullable = false)
    private int givenCount;

    @ColumnDefault("0")
    @Column(name = "reported_total_count", columnDefinition = "INT", nullable = false)
    private int reportedTotalCount;

    @Column(name = "stop_date", nullable = true)
    private LocalDateTime stopDate;

    @Column(name = "refresh_token", columnDefinition = "VARCHAR(255)")
    private String refreshToken;

    @OneToOne(mappedBy = "userInfo")
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserInfo(long id, int starTotal, int starCount, int ratingTotal, int ratingCount, String tier, double temperature, long visitCount, int giveCount, int givenCount, int reportedTotalCount, LocalDateTime stopDate, String refreshToken, User user) {
        this.id = id;
        this.starTotal = starTotal;
        this.starCount = starCount;
        this.ratingTotal = ratingTotal;
        this.ratingCount = ratingCount;
        this.tier = tier;
        this.temperature = temperature;
        this.visitCount = visitCount;
        this.giveCount = giveCount;
        this.givenCount = givenCount;
        this.reportedTotalCount = reportedTotalCount;
        this.stopDate = stopDate;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public void updateBanUser(LocalDateTime banDate) {
        this.reportedTotalCount += 1;
        this.stopDate = banDate;
    }

    public void plusGiveCount(){
        this.giveCount += 1;
    }

    public void updateStar(int starPoint) {
        this.starCount += 1;
        this.starTotal += starPoint;
    }

    public void updateRating(int ratingCount) {
        this.ratingCount += 1;
        this.ratingTotal += ratingCount;
    }

    public void updateTemperature(double temperature) {
        this.temperature += temperature;
    }
}