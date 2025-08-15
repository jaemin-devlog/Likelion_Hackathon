package org.likelion.hsu.likelion_hackathon.Dto.Response;

public class CopyPolishResponse {
    private String improvedText;

    public CopyPolishResponse() {}
    public CopyPolishResponse(String improvedText) { this.improvedText = improvedText; }

    public String getImprovedText() { return improvedText; }
    public void setImprovedText(String improvedText) { this.improvedText = improvedText;}
}