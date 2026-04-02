package ac.grim.reaperac.api.config;

public interface ChangeableConfig extends ConfigManager {

    void set(String key, Object value);

}
