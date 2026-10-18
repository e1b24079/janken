package oit.is.z3524.kaizi.janken.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JankenController {

  @GetMapping("/janken")
  public String janken() {
    return "janken.html";
  }

  @PostMapping("/janken")
  public String jankenName(@RequestParam String name, ModelMap model) {
    model.addAttribute("name", name);
    return "janken.html";
  }

  @GetMapping("/janken/game")
  public String jankenGame(@RequestParam String hand, ModelMap model) {
    String[] hands = { "Gu", "Choki", "Pa" };
    String cpuHand = hands[(int) (Math.random() * 3)];

    String result;
    if (hand.equals(cpuHand)) {
      result = "あいこ";
    } else if ((hand.equals("Gu") && cpuHand.equals("Choki"))
        || (hand.equals("Choki") && cpuHand.equals("Pa"))
        || (hand.equals("Pa") && cpuHand.equals("Gu"))) {
      result = "あなたの勝ち！";
    } else {
      result = "あなたの負け…";
    }

    model.addAttribute("myHand", hand);
    model.addAttribute("cpuHand", cpuHand);
    model.addAttribute("result", result);
    return "janken.html";
  }
}
