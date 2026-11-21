package dev.megaorbitals;

import java.io.File;
import java.lang.reflect.Modifier;

/**
 * Build-time tool (not used by the server): writes the default configs/<id>.yml for every cannon
 * so they can be bundled in the jar and seen in the project.
 * Usage: java -cp <classes + paper-api> dev.megaorbitals.ConfigExporter <classesDir> <outputDir>
 */
public final class ConfigExporter {
    public static void main(String[] args) throws Exception {
        File classes = new File(args[0], "dev/megaorbitals/cannons");
        File out = new File(args[1]);
        out.mkdirs();
        int n = 0;
        File[] files = classes.listFiles((d, name) -> name.endsWith(".class") && !name.contains("$"));
        if (files == null) throw new IllegalStateException("No compiled cannons in " + classes);
        for (File f : files) {
            try {
                Class<?> c = Class.forName("dev.megaorbitals.cannons." + f.getName().replace(".class", ""));
                if (!Cannon.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) continue;
                ((Cannon) c.getDeclaredConstructor().newInstance()).export(out);
                n++;
            } catch (Throwable t) {
                System.out.println("FAILED " + f.getName() + ": " + t);
            }
        }
        System.out.println("Exported " + n + " cannon configs to " + out);
    }
}
