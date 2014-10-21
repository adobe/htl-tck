package sightlytck.scripts.exprlang.xss;

public class XSSPojo {

    public String getAttributeContent() {
        return "Some \"attribute\" with quotes";
    }

    public String getHtmlContent() {
        return "<p style=\"color: red\">This is a red text.</p>";
    }

    public String getJavaScriptCode() {
        return "alert(null)";
    }

    public String getJavaScriptUri() {
        return "javascript:alert(null)";
    }

    public String getUriContent() {
        return "/sightlytck";
    }

}
