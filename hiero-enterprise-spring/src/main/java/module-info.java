module hiero.enterprise.spring {
  requires spring.context;
  requires com.openelements.hiero.base;
  requires org.jspecify;
  requires org.slf4j;
  requires spring.web;
  requires com.fasterxml.jackson.databind;
  requires spring.beans;
  requires spring.boot.autoconfigure;
  requires spring.boot;
  requires micrometer.core;

  exports com.openelements.hiero.spring;
}
