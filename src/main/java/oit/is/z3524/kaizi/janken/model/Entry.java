package oit.is.z3524.kaizi.janken.model;

import java.util.ArrayList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Entry {
  private ArrayList<String> users = new ArrayList<>();

  public void addUser(String name) {
    if (!users.contains(name)) {
      users.add(name);
    }
  }

  public ArrayList<String> getUsers() {
    return users;
  }

  public int getUserCount() {
    return users.size();
  }
}
