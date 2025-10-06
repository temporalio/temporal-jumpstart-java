package io.temporal.inframanager.domain.io.temporal.inframanager.domain.messages;

public class Workflows {
  public static class StartInfraSpaceRequest {
    public StartInfraSpaceRequest() {}

    public StartInfraSpaceRequest(String name) {
      this.name = name;
    }

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
