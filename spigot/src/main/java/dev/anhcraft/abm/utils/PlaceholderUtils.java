package dev.anhcraft.abm.utils;

import dev.anhcraft.jvmkit.utils.MathUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderUtils {
    private static final Pattern EXPRESSION_PLACEHOLDER_PATTERN = Pattern.compile("<\\?.+\\?>");
    private static final Pattern LOCALE_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([ A-Za-z0-9._\\-])+}}");
    private static final Pattern INFO_PLACEHOLDER_PATTERN = Pattern.compile("\\{__[a-zA-Z0-9_]+__}");

    public static String formatPAPI(Player player, String str){
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public static List<String> formatPAPI(Player player, List<String> str){
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public static String formatInfo(String str, Map<String, String> x){
        Matcher m = INFO_PLACEHOLDER_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer(str.length());
        while(m.find()){
            String p = m.group();
            String s = p.substring(3, p.length()-3).trim();
            m.appendReplacement(sb, x.getOrDefault(s, p));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String formatExpression(String str){
        Matcher m = EXPRESSION_PLACEHOLDER_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer(str.length());
        while(m.find()){
            String p = m.group();
            String s = p.substring(2, p.length()-2).trim();
            m.appendReplacement(sb, MathUtil.formatRound(new ExpressionBuilder(s).build().evaluate()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String localizeString(String str, ConfigurationSection localeConf){
        Matcher m = LOCALE_PLACEHOLDER_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer(str.length());
        while(m.find()){
            String p = m.group();
            String s = localeConf.getString(p.substring(2, p.length()-2).trim());
            m.appendReplacement(sb, s == null ? p : s);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static List<String> localizeStrings(List<String> strs, ConfigurationSection localeConf){
        ListIterator<String> it = strs.listIterator();
        while(it.hasNext()){
            String q = it.next();
            String s = q.trim();
            if(s.startsWith("$$")){
                it.remove();
                List<String> rpc = localeConf.getStringList(s.substring(2));
                for(String r : rpc) it.add(r);
            } else it.set(localizeString(q, localeConf));
        }
        return strs;
    }
}
