package com.timpo.common;

public class Constants {

  public class Defaults {

    public static final String COORDINATOR_ID = "DEFAULT_COORDINATOR_ID";
    public static final String BROKER_ID = "DEFAULT_BROKER_ID";
    //
    public static final int REDIS_BLOCK_TIMEOUT = 5; //seconds
  }

  public class TaskStates {

    public static final String PROCESSING = "processing";
    public static final String WAITING_FOR_RESOURCES = "waiting_for_resources";
    public static final String UNTOUCHED = "untouched";
  }
}
