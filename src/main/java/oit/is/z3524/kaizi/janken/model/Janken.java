package oit.is.z3524.kaizi.janken.model;

public class Janken {
  private String playerHand;
  private String cpuHand;
  private String result;

  public Janken(String hand) {
    this.playerHand = toJapanese(hand);
    this.cpuHand = "グー";
    this.result = judge(this.playerHand, this.cpuHand);
  }

  private String toJapanese(String hand) {
    switch (hand) {
      case "gu":
        return "グー";
      case "choki":
        return "チョキ";
      case "pa":
        return "パー";
      default:
        return "不明";
    }
  }

  private String judge(String player, String cpu) {
    if (player.equals(cpu)) {
      return "あいこ";
    }
    if ((player.equals("グー") && cpu.equals("チョキ"))
        || (player.equals("チョキ") && cpu.equals("パー"))
        || (player.equals("パー") && cpu.equals("グー"))) {
      return "あなたの勝ち";
    }
    return "あなたの負け";
  }

  public String getPlayerHand() {
    return playerHand;
  }

  public String getCpuHand() {
    return cpuHand;
  }

  public String getResult() {
    return result;
  }
}
