package sightlytck.scripts.blockstatements.attribute;

import java.util.Map;
import java.util.HashMap;

public class AttributesPojo  {

    private Map<String, String> attributes;
    private Map<String, String> rogueAttributes;
    private Map<String, String> rogueHref;

    public AttributesPojo() {
        attributes = new HashMap<String, String>();
        attributes.put("class", "foo");
        attributes.put("data-number", "2");

        rogueAttributes = new HashMap<String, String>();
        String injection = "><script>alert('busted')</script>";
        rogueAttributes.put(injection, injection);
        rogueAttributes.put("style", "color:red");
        rogueAttributes.put("onmouseover", "alert('PAWNED')");
        rogueAttributes.put("href='alert(\"PAWNED\")' data-href", "something");

        rogueHref = new HashMap<String, String>();
        rogueHref.put("href", "javascript:alert('foo')");
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Map<String, String> getRogueAttributes() {
        return rogueAttributes;
    }

    public Map<String, String> getRogueHref() {
        return rogueHref;
    }
}
