package org.likelion.hsu.likelion_hackathon.Dto.Request;

public class CopyPolishRequest {
    private String type;    // "STAY" | "TRANSFER"
    private String rawText; // 원문
    private String tone;    // 선택 (예: "따뜻하고 담백한 톤")

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
}