package ac.grim.grimac.utils.common.arguments;

import ac.grim.grimac.platform.api.Platform;

import static ac.grim.grimac.utils.common.arguments.ArgumentUtils.platform;
import static ac.grim.grimac.utils.common.arguments.ArgumentUtils.string;

public class CommonGrimArguments {

    private final static SystemArgumentFactory FACTORY = SystemArgumentFactory.Builder.of("Grim")
            .optionModifier(builder -> builder.key("Grim" + builder.options().getKey()))
            .supportEnv()
            .build();

    public final static SystemArgument<Boolean> KICK_ON_TRANSACTION_ERRORS = FACTORY.create(string("KickOnTransactionTaskErrors", false));
    public final static SystemArgument<String> API_URL = FACTORY.create(string("APIUrl", "https://api.grim.ac/v1/server/"));
    public final static SystemArgument<String> PASTE_URL = FACTORY.create(string("PasteUrl", "https://paste.grim.ac/"));
    public final static SystemArgument<Platform> PLATFORM_OVERRIDE = FACTORY.create(platform("PlatformOverride"));

}
