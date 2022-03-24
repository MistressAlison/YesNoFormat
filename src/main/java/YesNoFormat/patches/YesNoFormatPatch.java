package YesNoFormat.patches;

import YesNoFormat.YesNoFormatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SneckoField;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YesNoFormatPatch {

    public static final String YES = CardCrawlGame.languagePack.getUIString(YesNoFormatMod.makeID("Format")).TEXT[0];
    public static final String NO = CardCrawlGame.languagePack.getUIString(YesNoFormatMod.makeID("Format")).TEXT[1];
    public static final String MAYBE = CardCrawlGame.languagePack.getUIString(YesNoFormatMod.makeID("Format")).TEXT[2];

    private static String processString(String str) {
        //If the string we are trying to edit is not null///
        if (str != null) {
            String[] words = str.split(" ");
            //Iterate all words
            for (int i = 0 ; i < words.length ; i++) {
                //If the word doesnt have ! or [ or = or : or # (dynvars, icons, dyntext, keywords)
                if (!words[i].contains("!") && !words[i].contains("[") && !words[i].contains("=") && !words[i].contains(":")) {
                    //Compile a list of all numbers
                    ArrayList<String> numbers = new ArrayList<>();
                    Pattern p = Pattern.compile("\\d+");
                    Matcher m = p.matcher(words[i]);
                    while (m.find()) {
                        numbers.add(m.group());
                    }
                    //Replace the numbers with YES or NO
                    for (String num : numbers) {
                        if (NumberUtils.isCreatable(num)) {
                            Number n = NumberUtils.createNumber(num);
                            //If the value is not 0, input yes, else no
                            if (n.intValue() > 0) {
                                words[i] = words[i].replace(num, YES);
                            } else {
                                words[i] = words[i].replace(num, NO);
                            }
                        }
                    }
                }
            }
            str = String.join(" ", words);
        }
        return str;
    }

    private static String processXCost(String str) {
        //If the string we are trying to edit is not null///
        if (str != null) {
            //Define a string list for all the loneXs we find
            ArrayList<String> loneXs = new ArrayList<>();
            //This matches any lone X"
            Pattern p = Pattern.compile("\\b[X]\\b");
            Matcher m = p.matcher(str);
            //Add add the string matches to our list
            while (m.find()) {
                loneXs.add(m.group());
            }
            //Loop through our strings we found
            for (String s : loneXs) {
                str = str.replace(s, MAYBE);
            }
        }
        return str;
    }

    /*private static String processFoxString(String str) {
        if (str != null) {
            String[] words = str.split(" ");
            for (String s : words) {
                str = str.replace(s, "Fox");
            }
        }
        return str;
    }*/

    @SpirePatch(clz = BitmapFont.class, method = "draw", paramtypez = {Batch.class, CharSequence.class, float.class, float.class})
    public static class RuinAllFormattingPls1 {
        public static void Prefix(BitmapFont __instance, Batch batch, @ByRef CharSequence[] str, float x, float y) {
            str[0] = processString((String) str[0]);
        }
    }

    @SpirePatch(clz = BitmapFont.class, method = "draw", paramtypez = {Batch.class, CharSequence.class, float.class, float.class, float.class, int.class, boolean.class})
    public static class RuinAllFormattingPls2 {
        public static void Prefix(BitmapFont __instance, Batch batch, @ByRef CharSequence[] str, float x, float y, @ByRef float[] targetWidth, int halign, boolean wrap) {
            float before = str[0].length();
            str[0] = processString((String) str[0]);
            float after = str[0].length();
            targetWidth[0] *= (before/after);
        }
    }

    @SpirePatch(clz = BitmapFont.class, method = "draw", paramtypez = {Batch.class, CharSequence.class, float.class, float.class, int.class, int.class, float.class, int.class, boolean.class})
    public static class RuinAllFormattingPls3 {
        public static void Prefix(BitmapFont __instance, Batch batch, @ByRef CharSequence[] str, float x, float y, int start, int end, @ByRef float[] targetWidth, int halign, boolean wrap) {
            float before = str[0].length();
            str[0] = processString((String) str[0]);
            float after = str[0].length();
            targetWidth[0] *= (before/after);
        }
    }

    @SpirePatch(clz = BitmapFont.class, method = "draw", paramtypez = {Batch.class, CharSequence.class, float.class, float.class, int.class, int.class, float.class, int.class, boolean.class, String.class})
    public static class RuinAllFormattingPls4 {
        public static void Prefix(BitmapFont __instance, Batch batch, @ByRef CharSequence[] str, float x, float y, int start, int end, @ByRef float[] targetWidth, int halign, boolean wrap, String truncate) {
            float before = str[0].length();
            str[0] = processString((String) str[0]);
            float after = str[0].length();
            targetWidth[0] *= (before/after);
        }
    }

    @SpirePatch(clz = FontHelper.class, method = "renderSmartText", paramtypez = {SpriteBatch.class, BitmapFont.class, String.class, float.class, float.class, float.class, float.class, Color.class})
    public static class DontMessUpSpacingPls {
        @SpirePrefixPatch
        public static void pls(SpriteBatch sb, BitmapFont font, @ByRef String[] msg, float x, float y, float lineWidth, float lineSpacing, Color baseColor) {
            msg[0] = processString(msg[0]);
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderDynamicVariable")
    public static class FixVariablesPls {
        @SpireInsertPatch(locator = Locator.class, localvars = {"num"})
        public static void pls(AbstractCard __instance, char key, float start_x, float draw_y, int i, BitmapFont font, SpriteBatch sb, Character end, @ByRef StringBuilder[] ___sbuilder, int num) {
            ___sbuilder[0].setLength(0);
            ___sbuilder[0].append(processString(Integer.toString(num)));
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                com.evacipated.cardcrawl.modthespire.lib.Matcher finalMatcher = new com.evacipated.cardcrawl.modthespire.lib.Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.RenderCustomDynamicVariable.Inner.class, method = "myRenderDynamicVariable")
    @SpirePatch(clz = basemod.patches.com.megacrit.cardcrawl.screens.SingleCardViewPopup.RenderCustomDynamicVariable.Inner.class, method = "myRenderDynamicVariable")
    public static class FixVariablesForModdedCardsPls {
        @SpireInsertPatch(locator = Locator.class, localvars = {"stringBuilder","num"})
        public static void pls(Object __obj_instance, String key, char ckey, float start_x, float draw_y, int i, BitmapFont font, SpriteBatch sb, Character cend, @ByRef StringBuilder[] stringBuilder, int num) {
            stringBuilder[0].setLength(0);
            stringBuilder[0].append(processString(Integer.toString(num)));
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                com.evacipated.cardcrawl.modthespire.lib.Matcher finalMatcher = new com.evacipated.cardcrawl.modthespire.lib.Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    public static class FixHardcodedNumbersPls {
        @SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
        public static class DontMessUpSpacingPls {
            @SpirePrefixPatch
            public static void pls(@ByRef AbstractCard[] __instance) {
                __instance[0].rawDescription = processString(__instance[0].rawDescription);
                if (__instance[0].cost == -1 && YesNoFormatMod.enableMaybe) {
                    __instance[0].rawDescription = processXCost(__instance[0].rawDescription);
                }
            }
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderEnergy")
    public static class FixEnergyRenderPls {
        @SpireInsertPatch(locator = Locator.class, localvars = {"text"})
        public static void pls(AbstractCard __instance, SpriteBatch sb, @ByRef String[] text) {
            if (!SneckoField.snecko.get(__instance)) {
                if (NumberUtils.isCreatable(text[0])) {
                    Number n = NumberUtils.createNumber(text[0]);
                    if (n.intValue() > 0) {
                        text[0] = YES;
                    } else {
                        text[0] = NO;
                    }
                } else if (__instance.cost == -1 && YesNoFormatMod.enableMaybe) {
                    text[0] = MAYBE;
                }
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                com.evacipated.cardcrawl.modthespire.lib.Matcher finalMatcher = new com.evacipated.cardcrawl.modthespire.lib.Matcher.MethodCallMatcher(AbstractCard.class, "getEnergyFont");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = SingleCardViewPopup.class, method = "renderCost")
    public static class BeDifferentColorPls {
        @SpireInstrumentPatch
        public static ExprEditor patch() {
            return new ExprEditor() {
                @Override
                //Method call is basically the equivalent of a methodcallmatcher of an insert patch, checks the edit method against every method call in the function you#re patching
                public void edit(MethodCall m) throws CannotCompileException {
                    //If the method is from the class AnimationState and the method is called update
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFont")) {
                        m.replace("{" +
                                "if(!((Boolean)com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SneckoField.snecko.get(this.card)).booleanValue()) {" +
                                //This is usable and refers to the class you#re patching, can be substitued by $0 but that has extra rules
                                "if(YesNoFormat.YesNoFormatMod.enableMaybe && this.card.cost == -1) {" +
                                //$1 refers to the first input parameter of the method, in this case the float that Gdx.graphics.getDeltaTime() returns
                                "$3 = YesNoFormat.patches.YesNoFormatPatch.MAYBE;" +
                                "$4 = $4 - 128.0F * com.megacrit.cardcrawl.core.Settings.scale;" +
                                "}" +
                                "if(this.card.cost == 0) {" +
                                //$1 refers to the first input parameter of the method, in this case the float that Gdx.graphics.getDeltaTime() returns
                                "$3 = YesNoFormat.patches.YesNoFormatPatch.NO;" +
                                "$4 = $4 - 30.0F * com.megacrit.cardcrawl.core.Settings.scale;" +
                                "}" +
                                "if(this.card.cost > 0) {" +
                                //$1 refers to the first input parameter of the method, in this case the float that Gdx.graphics.getDeltaTime() returns
                                "$3 = YesNoFormat.patches.YesNoFormatPatch.YES;" +
                                "$4 = $4 - 60.0F * com.megacrit.cardcrawl.core.Settings.scale;" +
                                "}" +
                                "}" +
                                //Call the method as normal
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }
    }

    @SpirePatch2(clz = AbstractCard.class, method = "initializeDescriptionCN")
    public static class BetterWidthPls {
        @SpireInstrumentPatch
        public static ExprEditor patch() {
            return new ExprEditor() {
                @Override
                //Method call is basically the equivalent of a methodcallmatcher of an insert patch, checks the edit method against every method call in the function you#re patching
                public void edit(FieldAccess m) throws CannotCompileException {
                    //If the method is from the class AnimationState and the method is called update
                    if (m.getFieldName().equals("MAGIC_NUM_W")) {
                        m.replace("{" +
                                "$_ = 60.0F * com.megacrit.cardcrawl.core.Settings.scale;" +
                                //Call the method as normal
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = EnergyPanel.class, method = "render")
    public static class ShortenEnergyOrb {
        @SpireInsertPatch(locator = Locator.class, localvars = {"energyMsg"})
        public static void pls(EnergyPanel __instance, SpriteBatch sb, @ByRef String[] energyMsg) {
            if (EnergyPanel.totalCount > 0) {
                energyMsg[0] = YES;
            } else {
                energyMsg[0] = NO;
            }
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                com.evacipated.cardcrawl.modthespire.lib.Matcher finalMatcher = new com.evacipated.cardcrawl.modthespire.lib.Matcher.MethodCallMatcher(FontHelper.class, "renderFontCentered");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
