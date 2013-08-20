package com.revolsys.gis.wms.capabilities;

import java.net.URL;
import java.util.List;

public class Service {
  private String name;

  private String title;

  private String abstractDescription;

  private List<String> keywords;

  private URL onlineResource;

  private ContactInformation contactInformation;

  private String fees;

  private String accessConstraints;

  public String getAbstractDescription() {
    return abstractDescription;
  }

  public String getAccessConstraints() {
    return accessConstraints;
  }

  public ContactInformation getContactInformation() {
    return contactInformation;
  }

  public String getFees() {
    return fees;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public String getName() {
    return name;
  }

  public URL getOnlineResource() {
    return onlineResource;
  }

  public String getTitle() {
    return title;
  }

  public void setAbstractDescription(final String abstractDescription) {
    this.abstractDescription = abstractDescription;
  }

  public void setAccessConstraints(final String accessConstraints) {
    this.accessConstraints = accessConstraints;
  }

  public void setContactInformation(final ContactInformation contactInformation) {
    this.contactInformation = contactInformation;
  }

  public void setFees(final String fees) {
    this.fees = fees;
  }

  public void setKeywords(final List<String> keywords) {
    this.keywords = keywords;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOnlineResource(final URL onlineResource) {
    this.onlineResource = onlineResource;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
