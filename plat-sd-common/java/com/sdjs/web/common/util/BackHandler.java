package com.sdjs.web.common.util;

public interface BackHandler<T>
{ 
  public void handle(T aResult, Throwable aException); 
  
} 
