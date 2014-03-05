/* The following code was generated by JFlex 1.4.3 on 2/25/14 6:55 PM */

/*
 * Generated on 2/25/14 6:55 PM
 */
package fql.parse;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;


/**
 * 
 */
@SuppressWarnings("unused")
public class FqlTokenMaker extends AbstractJFlexCTokenMaker {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
 private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int EOL_COMMENT = 4;
  public static final int YYINITIAL = 0;
  public static final int MLC = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2, 2
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\17\1\7\1\0\1\17\1\15\22\0\1\17\1\24\1\14"+
    "\1\16\1\1\1\24\1\24\1\6\1\25\1\25\1\21\1\74\1\24"+
    "\1\75\1\23\1\20\1\4\1\72\1\72\1\4\4\4\2\3\1\36"+
    "\1\24\1\15\1\74\1\76\1\24\1\16\1\55\3\5\1\42\1\5"+
    "\1\65\1\1\1\60\2\1\1\64\1\66\1\62\2\1\1\40\1\43"+
    "\1\56\1\61\1\41\1\63\1\57\1\1\1\44\1\1\1\25\1\10"+
    "\1\25\1\77\1\2\1\0\1\47\1\13\1\50\1\67\1\35\1\32"+
    "\1\51\1\26\1\33\2\1\1\34\1\46\1\45\1\54\1\30\1\52"+
    "\1\12\1\31\1\27\1\11\1\70\1\37\1\71\1\53\1\73\1\22"+
    "\1\77\1\22\1\24\uff81\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\2\1\1\2\1\3\2\1\1\4\1\5\1\1"+
    "\1\6\1\7\23\1\1\10\1\11\5\10\1\12\3\10"+
    "\1\0\1\13\2\1\2\4\1\14\1\15\1\16\3\1"+
    "\1\17\33\1\1\20\11\0\3\1\1\4\1\21\1\4"+
    "\10\1\1\22\24\1\11\0\2\1\1\4\6\1\1\23"+
    "\16\1\2\0\1\24\2\0\1\25\1\0\1\1\1\4"+
    "\13\1\5\0\1\1\1\4\24\1";

  private static int [] zzUnpackAction() {
    int [] result = new int[209];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\100\0\200\0\300\0\u0100\0\u0140\0\300\0\u0180"+
    "\0\u01c0\0\u0200\0\u0240\0\u0280\0\300\0\300\0\u02c0\0\u0300"+
    "\0\u0340\0\u0380\0\u03c0\0\u0400\0\u0440\0\u0480\0\u04c0\0\u0500"+
    "\0\u0540\0\u0580\0\u05c0\0\u0600\0\u0640\0\u0680\0\u06c0\0\u0700"+
    "\0\u0740\0\u0780\0\300\0\u07c0\0\u0800\0\u0840\0\u0880\0\u08c0"+
    "\0\300\0\u0900\0\u0940\0\u0980\0\u09c0\0\u0a00\0\u0a40\0\u0a80"+
    "\0\u0ac0\0\u0b00\0\300\0\300\0\300\0\u0b40\0\u0b80\0\u0bc0"+
    "\0\u0100\0\u0c00\0\u0c40\0\u0c80\0\u0cc0\0\u0d00\0\u0d40\0\u0d80"+
    "\0\u0dc0\0\u0e00\0\u0e40\0\u0e80\0\u0ec0\0\u0f00\0\u0f40\0\u0f80"+
    "\0\u0fc0\0\u1000\0\u1040\0\u1080\0\u10c0\0\u1100\0\u1140\0\u1180"+
    "\0\u11c0\0\u1200\0\u1240\0\u1280\0\300\0\u12c0\0\u1300\0\u1340"+
    "\0\u1380\0\u13c0\0\u1400\0\u1440\0\u1480\0\u14c0\0\u1500\0\u1540"+
    "\0\u1580\0\u15c0\0\300\0\u1600\0\u1640\0\u1680\0\u16c0\0\u1700"+
    "\0\u1740\0\u1780\0\u17c0\0\u1800\0\u0100\0\u1840\0\u1880\0\u18c0"+
    "\0\u1900\0\u1940\0\u1980\0\u19c0\0\u1a00\0\u1a40\0\u1a80\0\u1ac0"+
    "\0\u1b00\0\u1b40\0\u1b80\0\u1bc0\0\u1c00\0\u1c40\0\u1c80\0\u1cc0"+
    "\0\u1d00\0\u1d40\0\u1d80\0\u1dc0\0\u1e00\0\u1e40\0\u1e80\0\u1ec0"+
    "\0\u1f00\0\u1f40\0\u1f80\0\u1fc0\0\u2000\0\u2040\0\u2080\0\u20c0"+
    "\0\u2100\0\u2140\0\u2180\0\u0100\0\u21c0\0\u2200\0\u2240\0\u2280"+
    "\0\u22c0\0\u2300\0\u2340\0\u2380\0\u23c0\0\u2400\0\u2440\0\u2480"+
    "\0\u24c0\0\u2500\0\u2540\0\u2580\0\u25c0\0\u2600\0\u2640\0\u2680"+
    "\0\u26c0\0\u2700\0\u2740\0\u2780\0\u27c0\0\u2800\0\u2840\0\u2880"+
    "\0\u28c0\0\u2900\0\u2940\0\u2980\0\u29c0\0\u2a00\0\u2a40\0\u25c0"+
    "\0\u2a80\0\u2680\0\u2ac0\0\u2b00\0\u2b40\0\u2b80\0\u2bc0\0\u2c00"+
    "\0\u2c40\0\u2c80\0\u2cc0\0\u2d00\0\u2d40\0\u2d80\0\u2dc0\0\u2e00"+
    "\0\u2e40\0\u2e80\0\u2ec0\0\u2f00\0\u2f40\0\u2f80\0\u2fc0\0\u3000"+
    "\0\u3040";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[209];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\2\5\2\6\1\5\1\4\1\7\1\4\1\10"+
    "\1\11\1\5\1\12\2\4\1\13\1\14\1\15\1\16"+
    "\2\4\1\16\1\5\1\17\1\20\1\21\1\22\1\23"+
    "\1\5\1\24\1\15\1\5\1\25\1\5\1\26\2\5"+
    "\1\27\1\30\1\31\1\32\1\5\1\33\1\5\1\34"+
    "\1\35\1\36\10\5\1\37\1\40\1\5\1\6\1\5"+
    "\1\15\1\41\1\4\1\15\7\42\1\43\11\42\1\44"+
    "\4\42\1\45\3\42\1\46\4\42\1\47\40\42\7\50"+
    "\1\51\16\50\1\52\3\50\1\53\4\50\1\54\40\50"+
    "\101\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\35\5\4\0\3\56\2\6\1\56\2\0\4\56\2\0"+
    "\1\56\7\0\10\56\1\0\33\56\1\6\1\56\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\10\5\1\0\6\5"+
    "\1\57\26\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\7\5\1\60\1\0\35\5\4\0\7\12\1\61\1\62"+
    "\3\12\1\63\63\12\17\0\1\13\100\0\1\64\1\65"+
    "\57\0\5\5\2\0\1\55\1\5\1\66\1\5\12\0"+
    "\1\67\7\5\1\0\35\5\5\0\5\5\2\0\1\55"+
    "\1\5\1\70\1\5\12\0\5\5\1\71\2\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\1\72\2\5\12\0"+
    "\1\5\1\73\3\5\1\74\2\5\1\0\6\5\1\75"+
    "\2\5\1\76\23\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\3\5\1\77\4\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\3\5\1\100\4\5\1\0"+
    "\6\5\1\101\21\5\1\71\4\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\6\5\1\102\4\5"+
    "\1\103\15\5\1\104\1\105\2\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\2\5\1\106\32\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\24\5\1\107\10\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\15\5\1\110\17\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\10\5\1\111"+
    "\24\5\5\0\5\5\2\0\1\55\1\5\1\112\1\5"+
    "\12\0\1\5\1\113\1\114\5\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\1\115\2\5\12\0\10\5\1\0"+
    "\15\5\1\116\17\5\5\0\5\5\2\0\1\55\1\117"+
    "\2\5\12\0\10\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\2\5\1\120\5\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\17\5\1\121\15\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\21\5\1\122\13\5\5\0\5\5"+
    "\2\0\1\55\1\5\1\70\1\5\12\0\7\5\1\123"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\15\5\1\124\17\5\102\0\1\15\1\0"+
    "\7\42\1\0\11\42\1\0\4\42\1\0\3\42\1\0"+
    "\4\42\1\0\40\42\20\0\1\125\106\0\1\126\77\0"+
    "\1\127\3\0\1\130\103\0\1\131\40\0\7\50\1\0"+
    "\16\50\1\0\3\50\1\0\4\50\1\0\40\50\27\0"+
    "\1\132\77\0\1\133\3\0\1\134\103\0\1\135\51\0"+
    "\1\136\66\0\6\56\2\0\4\56\2\0\1\56\7\0"+
    "\10\56\1\0\35\56\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\5\5\1\137\2\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\140\4\5\1\141"+
    "\1\5\1\0\35\5\4\0\10\61\1\142\3\61\1\143"+
    "\67\61\1\12\1\61\1\12\1\0\1\12\1\144\3\12"+
    "\12\61\1\12\2\61\1\12\12\61\1\12\24\61\1\12"+
    "\5\61\1\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\10\5\1\145\24\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\7\5\1\146\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\15\5\1\147"+
    "\17\5\5\0\5\5\2\0\1\55\2\5\1\150\12\0"+
    "\10\5\1\0\35\5\5\0\5\5\2\0\1\55\1\5"+
    "\1\151\1\5\12\0\10\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\12\5\1\152"+
    "\22\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\30\5\1\71\4\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\1\153\7\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\71\6\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\15\5\1\154\17\5\5\0\5\5\2\0\1\55"+
    "\1\5\1\71\1\5\12\0\1\5\1\155\1\5\1\156"+
    "\2\5\1\71\1\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\1\157\2\5\12\0\10\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\1\160\2\5\12\0\10\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\10\5\1\161\24\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\1\5\1\162\6\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\10\5\1\0\3\5"+
    "\1\163\31\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\16\5\1\164\16\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\30\5\1\165\4\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\1\5\1\166"+
    "\1\167\5\5\1\0\35\5\5\0\5\5\2\0\1\55"+
    "\1\5\1\170\1\5\12\0\10\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\1\5\1\171\6\5"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\2\5\1\172\5\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\1\5\1\173\1\5\12\0\10\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\1\5\1\174\1\5\12\0"+
    "\10\5\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\7\5\1\175\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\2\5\1\176\5\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\20\5\1\177\14\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\26\5\1\200\6\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\6\5\1\201\1\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\5\5"+
    "\1\75\2\5\1\0\35\5\33\0\1\202\100\0\1\203"+
    "\103\0\1\204\102\0\1\205\67\0\1\206\100\0\1\207"+
    "\103\0\1\210\102\0\1\211\43\0\3\212\5\0\1\212"+
    "\16\0\1\212\2\0\1\212\4\0\1\212\4\0\2\212"+
    "\4\0\1\212\11\0\1\212\2\0\1\212\6\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\71\6\5\1\0"+
    "\15\5\1\146\17\5\5\0\5\5\2\0\1\55\1\213"+
    "\2\5\12\0\10\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\10\5\1\214\24\5"+
    "\4\0\7\61\1\0\73\61\3\215\2\61\1\142\2\61"+
    "\1\215\1\143\15\61\1\215\2\61\1\215\4\61\1\215"+
    "\4\61\2\215\4\61\1\215\11\61\1\215\2\61\1\215"+
    "\5\61\1\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\6\5\1\216\26\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\10\5\1\0\6\5\1\71\26\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\2\5\1\71\5\5"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\3\5\1\217\4\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\5\5\1\220\2\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\7\5\1\221\25\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\7\5\1\222\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\33\5\1\71\1\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\1\5\1\223"+
    "\6\5\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\7\5\1\224\25\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\10\5\1\225"+
    "\24\5\5\0\5\5\2\0\1\55\3\5\12\0\6\5"+
    "\1\71\1\5\1\0\35\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\7\5\1\226\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\4\5\1\227"+
    "\30\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\25\5\1\71\7\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\7\5\1\230\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\11\5\1\231"+
    "\23\5\5\0\5\5\2\0\1\55\3\5\12\0\2\5"+
    "\1\232\5\5\1\0\35\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\10\5\1\0\15\5\1\233\17\5\5\0"+
    "\5\5\2\0\1\55\1\5\1\234\1\5\12\0\10\5"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\6\5\1\235\1\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\1\5\1\235\1\5\12\0\10\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\7\5\1\236"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\1\5\1\237"+
    "\1\5\12\0\10\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\15\5\1\240\17\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\4\5\1\241\30\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\27\5\1\242\5\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\221\6\5\1\0"+
    "\35\5\34\0\1\243\105\0\1\244\76\0\1\203\65\0"+
    "\1\245\104\0\1\246\105\0\1\247\76\0\1\207\65\0"+
    "\1\250\57\0\3\251\5\0\1\251\16\0\1\251\2\0"+
    "\1\251\4\0\1\251\4\0\2\251\4\0\1\251\11\0"+
    "\1\251\2\0\1\251\6\0\5\5\2\0\1\55\1\5"+
    "\1\146\1\5\12\0\10\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\252\6\5\1\0"+
    "\35\5\4\0\3\61\3\253\2\61\1\142\2\61\1\253"+
    "\1\143\15\61\1\253\2\61\1\253\4\61\1\253\4\61"+
    "\2\253\4\61\1\253\11\61\1\253\2\61\1\253\5\61"+
    "\1\0\5\5\2\0\1\55\3\5\12\0\3\5\1\254"+
    "\4\5\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\11\5\1\255\23\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\6\5\1\256"+
    "\26\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\10\5\1\71\24\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\10\5\1\0\7\5\1\257\25\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\10\5\1\0\10\5"+
    "\1\260\24\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\1\5\1\261\6\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\1\5\1\262\1\5\12\0\10\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\5\5\1\224\27\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\3\5\1\71\4\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\71\7\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\5\5\1\263"+
    "\2\5\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\1\230\34\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\5\5\1\264\2\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\14\5\1\71\20\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\1\5\1\140\6\5\1\0\35\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\14\5\1\224"+
    "\20\5\5\0\5\5\2\0\1\55\3\5\12\0\3\5"+
    "\1\265\4\5\1\0\35\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\10\5\1\0\21\5\1\266\13\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\10\5\1\0\16\5"+
    "\1\71\16\5\35\0\1\203\4\0\1\244\61\0\1\267"+
    "\60\0\1\245\1\270\3\245\1\270\2\0\3\245\2\0"+
    "\1\270\1\0\1\245\1\270\1\0\3\270\10\245\1\270"+
    "\35\245\2\270\33\0\1\207\4\0\1\247\61\0\1\271"+
    "\60\0\1\250\1\272\3\250\1\272\2\0\3\250\2\0"+
    "\1\272\1\0\1\250\1\272\1\0\3\272\10\250\1\272"+
    "\35\250\2\272\5\0\3\273\5\0\1\273\16\0\1\273"+
    "\2\0\1\273\4\0\1\273\4\0\2\273\4\0\1\273"+
    "\11\0\1\273\2\0\1\273\6\0\5\5\2\0\1\55"+
    "\3\5\12\0\5\5\1\274\2\5\1\0\35\5\4\0"+
    "\3\61\3\275\2\61\1\142\2\61\1\275\1\143\15\61"+
    "\1\275\2\61\1\275\4\61\1\275\4\61\2\275\4\61"+
    "\1\275\11\61\1\275\2\61\1\275\5\61\1\0\5\5"+
    "\2\0\1\55\3\5\12\0\4\5\1\276\3\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\1\277"+
    "\7\5\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\12\5\1\155\22\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\10\5\1\224"+
    "\24\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\6\5\1\300\26\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\5\5\1\301\2\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\10\5\1\0\6\5"+
    "\1\104\26\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\6\5\1\302\26\5\5\0\5\5\2\0"+
    "\1\55\2\5\1\303\12\0\10\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\5\5\1\304\2\5"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\22\5\1\305\12\5\24\0\1\245\77\0"+
    "\1\250\62\0\3\5\5\0\1\5\16\0\1\5\2\0"+
    "\1\5\4\0\1\5\4\0\2\5\4\0\1\5\11\0"+
    "\1\5\2\0\1\5\6\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\15\5\1\306\17\5\4\0\3\61"+
    "\3\12\2\61\1\142\2\61\1\12\1\143\15\61\1\12"+
    "\2\61\1\12\4\61\1\12\4\61\2\12\4\61\1\12"+
    "\11\61\1\12\2\61\1\12\5\61\1\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\15\5\1\307\17\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\7\5\1\152"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\11\5\1\310\23\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\10\5\1\0\15\5\1\311\17\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\12\5\1\224\22\5\5\0\5\5\2\0\1\55\1\312"+
    "\2\5\12\0\10\5\1\0\35\5\5\0\5\5\2\0"+
    "\1\55\3\5\12\0\1\5\1\313\6\5\1\0\35\5"+
    "\5\0\5\5\2\0\1\55\3\5\12\0\10\5\1\0"+
    "\22\5\1\314\12\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\6\5\1\315\26\5\5\0\5\5"+
    "\2\0\1\55\1\5\1\157\1\5\12\0\10\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\7\5"+
    "\1\224\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\6\5\1\230\26\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\1\5\1\165\6\5\1\0"+
    "\35\5\5\0\5\5\2\0\1\55\3\5\12\0\7\5"+
    "\1\71\1\0\35\5\5\0\5\5\2\0\1\55\3\5"+
    "\12\0\10\5\1\0\3\5\1\316\31\5\5\0\5\5"+
    "\2\0\1\55\3\5\12\0\10\5\1\0\10\5\1\317"+
    "\24\5\5\0\5\5\2\0\1\55\3\5\12\0\10\5"+
    "\1\0\23\5\1\71\11\5\5\0\5\5\2\0\1\55"+
    "\3\5\12\0\6\5\1\320\1\5\1\0\35\5\5\0"+
    "\5\5\2\0\1\55\3\5\12\0\5\5\1\321\2\5"+
    "\1\0\35\5\5\0\5\5\2\0\1\55\3\5\12\0"+
    "\10\5\1\0\34\5\1\313\4\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[12416];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\1\11\2\1\1\11\5\1\2\11\24\1\1\11"+
    "\5\1\1\11\3\1\1\0\5\1\3\11\37\1\1\11"+
    "\11\0\4\1\1\11\36\1\11\0\30\1\2\0\1\1"+
    "\2\0\1\1\1\0\15\1\5\0\26\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[209];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */


	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public FqlTokenMaker() {
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
	private void addHyperlinkToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, true);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addHyperlinkToken(int, int, int)
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *        occurs.
	 * @param hyperlink Whether this token is a hyperlink.
	 */
	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Returns the text to place at the beginning and end of a
	 * line to "comment" it in a this programming language.
	 *
	 * @return The start and end strings to add to a line to "comment"
	 *         it out.
	 */
	public String[] getLineCommentStartAndEnd() {
		return new String[] { "//", null };
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;

		// Start off in the proper state.
		int state = Token.NULL;
		switch (initialTokenType) {
						case Token.COMMENT_MULTILINE:
				state = MLC;
				start = text.offset;
				break;

			/* No documentation comments */
			default:
				state = Token.NULL;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null; //new Token();
		}

	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}




  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public FqlTokenMaker(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public FqlTokenMaker(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 188) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public org.fife.ui.rsyntaxtextarea.Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 3: 
          { addNullToken(); return firstToken;
          }
        case 22: break;
        case 14: 
          { start = zzMarkedPos-2; yybegin(MLC);
          }
        case 23: break;
        case 5: 
          { addToken(Token.WHITESPACE);
          }
        case 24: break;
        case 17: 
          { addToken(Token.ERROR_STRING_DOUBLE);
          }
        case 25: break;
        case 19: 
          { addToken(Token.RESERVED_WORD);
          }
        case 26: break;
        case 7: 
          { addToken(Token.SEPARATOR);
          }
        case 27: break;
        case 1: 
          { addToken(Token.IDENTIFIER);
          }
        case 28: break;
        case 10: 
          { addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken;
          }
        case 29: break;
        case 13: 
          { start = zzMarkedPos-2; yybegin(EOL_COMMENT);
          }
        case 30: break;
        case 4: 
          { addToken(Token.ERROR_STRING_DOUBLE); addNullToken(); return firstToken;
          }
        case 31: break;
        case 18: 
          { addToken(Token.DATA_TYPE);
          }
        case 32: break;
        case 16: 
          { yybegin(YYINITIAL); addToken(start,zzStartRead+2-1, Token.COMMENT_MULTILINE);
          }
        case 33: break;
        case 12: 
          { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE);
          }
        case 34: break;
        case 21: 
          { int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_EOL); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_EOL); start = zzMarkedPos;
          }
        case 35: break;
        case 20: 
          { int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_MULTILINE); start = zzMarkedPos;
          }
        case 36: break;
        case 15: 
          { addToken(Token.RESERVED_WORD_2);
          }
        case 37: break;
        case 11: 
          { addToken(Token.ERROR_NUMBER_FORMAT);
          }
        case 38: break;
        case 2: 
          { addToken(Token.LITERAL_NUMBER_DECIMAL_INT);
          }
        case 39: break;
        case 6: 
          { addToken(Token.OPERATOR);
          }
        case 40: break;
        case 8: 
          { 
          }
        case 41: break;
        case 9: 
          { addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken;
          }
        case 42: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            switch (zzLexicalState) {
            case EOL_COMMENT: {
              addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken;
            }
            case 210: break;
            case YYINITIAL: {
              addNullToken(); return firstToken;
            }
            case 211: break;
            case MLC: {
              addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken;
            }
            case 212: break;
            default:
            return null;
            }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
