package io.temporal.inframanager.domain.messages;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      StartInfraSpaceRequest that = (StartInfraSpaceRequest) o;
      return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(getName());
    }
  }

  public static class GetInfraSpaceStateResponse {
    private String name;
    private StartInfraSpaceRequest args;

    public GetInfraSpaceStateResponse() {}

    public GetInfraSpaceStateResponse(StartInfraSpaceRequest args) {
      this.args = args;
      this.name = args.getName();
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public StartInfraSpaceRequest getArgs() {
      return args;
    }

    public void setArgs(StartInfraSpaceRequest args) {
      this.args = args;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      GetInfraSpaceStateResponse that = (GetInfraSpaceStateResponse) o;
      return Objects.equals(getName(), that.getName()) && Objects.equals(getArgs(), that.getArgs());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getName(), getArgs());
    }
  }
}
