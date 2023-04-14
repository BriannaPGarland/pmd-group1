
 package net.sourceforge.pmd.renderers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.util.AssertionUtil;

public final class RendererFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RendererFactory.class);

    private static final Map<String, Function<Properties, Renderer>> RENDERERS;

    static {
        RENDERERS = Map.of(
            CodeClimateRenderer.NAME, CodeClimateRenderer::new,
            XMLRenderer.NAME, XMLRenderer::new,
            IDEAJRenderer.NAME, IDEAJRenderer::new,
            TextColorRenderer.NAME, TextColorRenderer::new,
            TextRenderer.NAME, TextRenderer::new,
            TextPadRenderer.NAME, TextPadRenderer::new,
            EmacsRenderer.NAME, EmacsRenderer::new,
            CSVRenderer.NAME, CSVRenderer::new,
            HTMLRenderer.NAME, HTMLRenderer::new,
            XSLTRenderer.NAME, XSLTRenderer::new,
            YAHTMLRenderer.NAME, YAHTMLRenderer::new,
            SummaryHTMLRenderer.NAME, SummaryHTMLRenderer::new,
            VBHTMLRenderer.NAME, VBHTMLRenderer::new,
            EmptyRenderer.NAME, EmptyRenderer::new,
            JsonRenderer.NAME, JsonRenderer::new,
            SarifRenderer.NAME, SarifRenderer::new
        );
    }

    private RendererFactory() {}

    public static Set<String> supportedRenderers() {
        return Collections.unmodifiableSet(RENDERERS.keySet());
    }

    public static Renderer createRenderer(String reportFormat, Properties properties) {
        AssertionUtil.requireParamNotNull("reportFormat", reportFormat);
        Function<Properties, Renderer> rendererConstructor = getRendererConstructor(reportFormat);
        Renderer renderer = rendererConstructor.apply(properties);
        if (RENDERERS.containsKey(reportFormat) && !reportFormat.equals(renderer.getName())) {
            LOG.warn("Report format '{}' is deprecated, and has been replaced with '{}'. "
                    + "Future versions of PMD will remove support for this deprecated Report format usage.",
                    reportFormat, renderer.getName());
        }
        return renderer;
    }

    private static Function<Properties, Renderer> getRendererConstructor(String reportFormat) {
        AssertionUtil.requireParamNotNull("reportFormat", reportFormat);
        Function<Properties, Renderer> rendererConstructor = RENDERERS.get(reportFormat);

        if (rendererConstructor == null && !"".equals(reportFormat)) {
            try {
                Class<?> clazz = Class.forName(reportFormat);
                if (!Renderer.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Custom report renderer class does not implement the "
                            + Renderer.class.getName() + " interface.");
                } else {
                    Constructor<?> ctor = clazz.getConstructor();
                    ctor.setAccessible(true);
                    rendererConstructor = properties -> {
                        try {
                            return (Renderer) ctor.newInstance();
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException e) {
                            throw new RuntimeException("Unable to instantiate Renderer", e);
                        }
                    };
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Can't find the custom format " + reportFormat + ": " + e);
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (rendererConstructor == null) {
            throw new IllegalArgumentException("Unknown renderer format " + reportFormat);
        }
        return rendererConstructor;
    }
}
