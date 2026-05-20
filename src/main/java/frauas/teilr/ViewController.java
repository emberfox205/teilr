package frauas.teilr;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/* WARNING: DO NOT TOUCH */

@Controller
public class ViewController {

    // 1. This loads your main mobile frame when you visit http://localhost:8080/
    @GetMapping("/")
    public String index(Model model) {
        // This tells layout.html to load "fragments/home.html" as its initial scene
        model.addAttribute("initialView", "fragments/home :: sceneContent");
        return "layout";
    }

    // 2. Add temporary, simple endpoints for your nav buttons to swap scenes
    @GetMapping("/ui/home")
    public String getHomeScene() {
        return "fragments/home :: sceneContent";
    }

    @GetMapping("/ui/profile")
    public String getProfileScene() {
        return "fragments/profile :: sceneContent";
    }

    @GetMapping("/ui/settings")
    public String getSettingsScene() {
        return "fragments/settings :: sceneContent";
    }
}
