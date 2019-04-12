package net.explorviz.settings.services;


import com.mongodb.WriteResult;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import net.explorviz.settings.model.Setting;
import net.explorviz.shared.security.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morphia.Datastore;
import xyz.morphia.query.Query;

/**
 * This service is responsible for persisting and retrieving {@link Setting} objects. 
 * It is backed by mongodb.
 * 
 */
public class SettingsService {

  
  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsService.class.getSimpleName());

  
  
  private final Datastore datastore;
  
  @Inject
  public SettingsService(Datastore datastore) {
    this.datastore = datastore;
  }

  /**
   * Queries all settings
   * @return a list of all settings
   */
  public List<Setting> findAll() {
    Query<Setting> q = this.datastore.find(Setting.class);
    return q.asList();
  }
  
  /**
   * Queries a single setting
   * @param id the id
   * @return the setting
   */
  public Optional<Setting> findById(String id) {
    Setting s = this.datastore.get(Setting.class, id);
    return Optional.ofNullable(s);
  }
  
  /**
   * Removes a setting with a given id 
   * @param id the id
   */
  public void delete(String id) {
    WriteResult wr = this.datastore.delete(Setting.class, id);
    if(LOGGER.isInfoEnabled()) {
      if (wr.getN() > 0) {
        LOGGER.info(String.format("Removed setting with id {}", id));
      } else {
        LOGGER.info(String.format("No setting with id {}", id));
      }
    }
  }
  
  /**
   * Creates a new setting
   * @param setting the setting to create
   */
  public void save(Setting setting) {
    this.datastore.save(setting);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(String.format("Saved setting with id {}", setting.getId()));
    }
  }
  
}