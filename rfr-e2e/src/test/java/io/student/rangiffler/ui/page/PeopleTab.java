package io.student.rangiffler.ui.page;

public enum PeopleTab {
  FRIENDS("Friends", "GetFriends"),
  ALL_PEOPLE("All People", "GetPeople"),
  OUTCOME_INVITATIONS("Outcome invitations", "GetOutcomeInvitations"),
  INCOME_INVITATIONS("Income invitations", "GetInvitations");

  private final String label;
  private final String operation;

  PeopleTab(String label, String operation) {
    this.label = label;
    this.operation = operation;
  }

  String label() {
    return label;
  }

  String operation() {
    return operation;
  }
}
