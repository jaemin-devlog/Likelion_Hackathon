package org.likelion.hsu.likelion_hackathon.Dto.Response;

public class TransferTopItem {
    private Long id;
    private String thumbnailUrl;  // 대표사진 1장
    private Integer price;        // 총 금액(원)

    public TransferTopItem() {}

    public TransferTopItem(Long id, String thumbnailUrl, Integer price) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
