/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.database;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.lang.Language;

/**
 * Instantiate the fields required to retrieve {@link SourceCode}.
 *
 * @author sturton
 */
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Data
public class SourceObject {

    private static final Logger LOG = LoggerFactory.getLogger(SourceObject.class);

    private String schema;
    private String name;
    private String type;
    private String revision;

    public String getPseudoFileName() {
        StringBuilder builder = new StringBuilder();
        builder.append("/Database/")
                .append(getSchema())
                .append("/")
                .append(getType())
                .append("/")
                .append(getName())
                .append(getSuffixFromType());
        return builder.toString();
    }

    private String getSuffixFromType() {
        String suffix = "";
        if (type != null && !type.isEmpty()) {
            switch (type.toUpperCase(Locale.ROOT)) {
                case "JAVA_SOURCE":
                    suffix = ".java";
                    break;
                case "TRIGGER":
                    suffix = ".trg";
                    break;
                case "FUNCTION":
                    suffix = ".fnc";
                    break;
                case "PROCEDURE":
                    suffix = ".prc";
                    break;
                case "PACKAGE_BODY":
                    suffix = ".pkb";
                    break;
                case "PACKAGE":
                    suffix = ".pks";
                    break;
                case "TYPE_BODY":
                    suffix = ".tpb";
                    break;
                case "TYPE":
                    suffix = ".tps";
                    break;
                default:
                    suffix = "";
                    break;
            }
        }
        return suffix;
    }

}
