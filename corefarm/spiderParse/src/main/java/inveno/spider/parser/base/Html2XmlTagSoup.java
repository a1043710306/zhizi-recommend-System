package inveno.spider.parser.base;


import inveno.spider.parser.exception.ExtractException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class Html2XmlTagSoup {
	//这个MAP只用来防止里面的KEY被做&到&amp;的转义，没其它用处。已经移到 HtmlEntries.entry_set
    //private static HashMap<String, String> htmlEntities; 
    
    private static final int maxLength;
    static {
        //htmlEntities = new HashMap<String, String>();
        /*
        htmlEntities.put("&lt;", "<");
        htmlEntities.put("&gt;", ">");
        htmlEntities.put("&amp;", "&");
        htmlEntities.put("&quot;", "\"");
        htmlEntities.put("&agrave;", "à");
        htmlEntities.put("&Agrave;", "À");
        htmlEntities.put("&acirc;", "â");
        htmlEntities.put("&auml;", "ä");
        htmlEntities.put("&Auml;", "Ä");
        htmlEntities.put("&Acirc;", "Â");
        htmlEntities.put("&aring;", "å");
        htmlEntities.put("&Aring;", "Å");
        htmlEntities.put("&aelig;", "æ");
        htmlEntities.put("&AElig;", "Æ");
        htmlEntities.put("&ccedil;", "ç");
        htmlEntities.put("&Ccedil;", "Ç");
        htmlEntities.put("&eacute;", "é");
        htmlEntities.put("&Eacute;", "É");
        htmlEntities.put("&egrave;", "è");
        htmlEntities.put("&Egrave;", "È");
        htmlEntities.put("&ecirc;", "ê");
        htmlEntities.put("&Ecirc;", "Ê");
        htmlEntities.put("&euml;", "ë");
        htmlEntities.put("&Euml;", "Ë");
        htmlEntities.put("&iuml;", "ï");
        htmlEntities.put("&Iuml;", "Ï");
        htmlEntities.put("&ocirc;", "ô");
        htmlEntities.put("&Ocirc;", "Ô");
        htmlEntities.put("&ouml;", "ö");
        htmlEntities.put("&Ouml;", "Ö");
        htmlEntities.put("&oslash;", "ø");
        htmlEntities.put("&Oslash;", "Ø");
        htmlEntities.put("&szlig;", "ß");
        htmlEntities.put("&ugrave;", "ù");
        htmlEntities.put("&Ugrave;", "Ù");
        htmlEntities.put("&ucirc;", "û");
        htmlEntities.put("&Ucirc;", "Û");
        htmlEntities.put("&uuml;", "ü");
        htmlEntities.put("&Uuml;", "Ü");
        htmlEntities.put("&nbsp;", " ");
        htmlEntities.put("&copy;", "\u00a9");
        htmlEntities.put("&reg;", "\u00ae");
        htmlEntities.put("&euro;", "\u20a0");
        
        //新添加的
        htmlEntities.put("&middot;", "·");
        htmlEntities.put("&rdquo;", "”");
        htmlEntities.put("&ldquo;", "“");
        */
        
    	/* moved to HtmlEntries.entry_set;
        //all html charactor code.
        htmlEntities.put("&quot;".toLowerCase(),"	quotation mark");
        htmlEntities.put("&apos;".toLowerCase(),"apostrophe ");
        htmlEntities.put("&amp;".toLowerCase(),"	ampersand");
        htmlEntities.put("&lt;".toLowerCase(),"	less-than");
        htmlEntities.put("&gt;".toLowerCase(),"	greater-than");
        htmlEntities.put("&nbsp;".toLowerCase(),"	non-breaking space");
        htmlEntities.put("&iexcl;".toLowerCase(),"	inverted exclamation mark");
        htmlEntities.put("&cent;".toLowerCase(),"	cent");
        htmlEntities.put("&pound;".toLowerCase(),"	pound");
        htmlEntities.put("&curren;".toLowerCase(),"	currency");
        htmlEntities.put("&yen;".toLowerCase(),"	yen");
        htmlEntities.put("&brvbar;".toLowerCase(),"	broken vertical bar");
        htmlEntities.put("&sect;".toLowerCase(),"	section");
        htmlEntities.put("&uml;".toLowerCase(),"	spacing diaeresis");
        htmlEntities.put("&copy;".toLowerCase(),"	copyright");
        htmlEntities.put("&ordf;".toLowerCase(),"	feminine ordinal indicator");
        htmlEntities.put("&laquo;".toLowerCase(),"	angle quotation mark (left)");
        htmlEntities.put("&not;".toLowerCase(),"	negation");
        htmlEntities.put("&shy;".toLowerCase(),"	soft hyphen");
        htmlEntities.put("&reg;".toLowerCase(),"	registered trademark");
        htmlEntities.put("&macr;".toLowerCase(),"	spacing macron");
        htmlEntities.put("&deg;".toLowerCase(),"	degree");
        htmlEntities.put("&plusmn;".toLowerCase(),"	plus-or-minus ");
        htmlEntities.put("&sup2;".toLowerCase(),"	superscript 2");
        htmlEntities.put("&sup3;".toLowerCase(),"	superscript 3");
        htmlEntities.put("&acute;".toLowerCase(),"	spacing acute");
        htmlEntities.put("&micro;".toLowerCase(),"	micro");
        htmlEntities.put("&para;".toLowerCase(),"	paragraph");
        htmlEntities.put("&middot;".toLowerCase(),"	middle dot");
        htmlEntities.put("&cedil;".toLowerCase(),"	spacing cedilla");
        htmlEntities.put("&sup1;".toLowerCase(),"	superscript 1");
        htmlEntities.put("&ordm;".toLowerCase(),"	masculine ordinal indicator");
        htmlEntities.put("&raquo;".toLowerCase(),"	angle quotation mark (right)");
        htmlEntities.put("&frac14;".toLowerCase(),"	fraction 1/4");
        htmlEntities.put("&frac12;".toLowerCase(),"	fraction 1/2");
        htmlEntities.put("&frac34;".toLowerCase(),"	fraction 3/4");
        htmlEntities.put("&iquest;".toLowerCase(),"	inverted question mark");
        htmlEntities.put("&times;".toLowerCase(),"	multiplication");
        htmlEntities.put("&divide;".toLowerCase(),"	division");
        htmlEntities.put("&Agrave;".toLowerCase(),"	capital a, grave accent");
        htmlEntities.put("&Aacute;".toLowerCase(),"	capital a, acute accent");
        htmlEntities.put("&Acirc;".toLowerCase(),"	capital a, circumflex accent");
        htmlEntities.put("&Atilde;".toLowerCase(),"	capital a, tilde");
        htmlEntities.put("&Auml;".toLowerCase(),"	capital a, umlaut mark");
        htmlEntities.put("&Aring;".toLowerCase(),"	capital a, ring");
        htmlEntities.put("&AElig;".toLowerCase(),"	capital ae");
        htmlEntities.put("&Ccedil;".toLowerCase(),"	capital c, cedilla");
        htmlEntities.put("&Egrave;".toLowerCase(),"	capital e, grave accent");
        htmlEntities.put("&Eacute;".toLowerCase(),"	capital e, acute accent");
        htmlEntities.put("&Ecirc;".toLowerCase(),"	capital e, circumflex accent");
        htmlEntities.put("&Euml;".toLowerCase(),"	capital e, umlaut mark");
        htmlEntities.put("&Igrave;".toLowerCase(),"	capital i, grave accent");
        htmlEntities.put("&Iacute;".toLowerCase(),"	capital i, acute accent");
        htmlEntities.put("&Icirc;".toLowerCase(),"	capital i, circumflex accent");
        htmlEntities.put("&Iuml;".toLowerCase(),"	capital i, umlaut mark");
        htmlEntities.put("&ETH;".toLowerCase(),"	capital eth, Icelandic");
        htmlEntities.put("&Ntilde;".toLowerCase(),"	capital n, tilde");
        htmlEntities.put("&Ograve;".toLowerCase(),"	capital o, grave accent");
        htmlEntities.put("&Oacute;".toLowerCase(),"	capital o, acute accent");
        htmlEntities.put("&Ocirc;".toLowerCase(),"	capital o, circumflex accent");
        htmlEntities.put("&Otilde;".toLowerCase(),"	capital o, tilde");
        htmlEntities.put("&Ouml;".toLowerCase(),"	capital o, umlaut mark");
        htmlEntities.put("&Oslash;".toLowerCase(),"	capital o, slash");
        htmlEntities.put("&Ugrave;".toLowerCase(),"	capital u, grave accent");
        htmlEntities.put("&Uacute;".toLowerCase(),"	capital u, acute accent");
        htmlEntities.put("&Ucirc;".toLowerCase(),"	capital u, circumflex accent");
        htmlEntities.put("&Uuml;".toLowerCase(),"	capital u, umlaut mark");
        htmlEntities.put("&Yacute;".toLowerCase(),"	capital y, acute accent");
        htmlEntities.put("&THORN;".toLowerCase(),"	capital THORN, Icelandic");
        htmlEntities.put("&szlig;".toLowerCase(),"	small sharp s, German");
        htmlEntities.put("&agrave;".toLowerCase(),"	small a, grave accent");
        htmlEntities.put("&aacute;".toLowerCase(),"	small a, acute accent");
        htmlEntities.put("&acirc;".toLowerCase(),"	small a, circumflex accent");
        htmlEntities.put("&atilde;".toLowerCase(),"	small a, tilde");
        htmlEntities.put("&auml;".toLowerCase(),"	small a, umlaut mark");
        htmlEntities.put("&aring;".toLowerCase(),"	small a, ring");
        htmlEntities.put("&aelig;".toLowerCase(),"	small ae");
        htmlEntities.put("&ccedil;".toLowerCase(),"	small c, cedilla");
        htmlEntities.put("&egrave;".toLowerCase(),"	small e, grave accent");
        htmlEntities.put("&eacute;".toLowerCase(),"	small e, acute accent");
        htmlEntities.put("&ecirc;".toLowerCase(),"	small e, circumflex accent");
        htmlEntities.put("&euml;".toLowerCase(),"	small e, umlaut mark");
        htmlEntities.put("&igrave;".toLowerCase(),"	small i, grave accent");
        htmlEntities.put("&iacute;".toLowerCase(),"	small i, acute accent");
        htmlEntities.put("&icirc;".toLowerCase(),"	small i, circumflex accent");
        htmlEntities.put("&iuml;".toLowerCase(),"	small i, umlaut mark");
        htmlEntities.put("&eth;".toLowerCase(),"	small eth, Icelandic");
        htmlEntities.put("&ntilde;".toLowerCase(),"	small n, tilde");
        htmlEntities.put("&ograve;".toLowerCase(),"	small o, grave accent");
        htmlEntities.put("&oacute;".toLowerCase(),"	small o, acute accent");
        htmlEntities.put("&ocirc;".toLowerCase(),"	small o, circumflex accent");
        htmlEntities.put("&otilde;".toLowerCase(),"	small o, tilde");
        htmlEntities.put("&ouml;".toLowerCase(),"	small o, umlaut mark");
        htmlEntities.put("&oslash;".toLowerCase(),"	small o, slash");
        htmlEntities.put("&ugrave;".toLowerCase(),"	small u, grave accent");
        htmlEntities.put("&uacute;".toLowerCase(),"	small u, acute accent");
        htmlEntities.put("&ucirc;".toLowerCase(),"	small u, circumflex accent");
        htmlEntities.put("&uuml;".toLowerCase(),"	small u, umlaut mark");
        htmlEntities.put("&yacute;".toLowerCase(),"	small y, acute accent");
        htmlEntities.put("&thorn;".toLowerCase(),"	small thorn, Icelandic");
        htmlEntities.put("&yuml;".toLowerCase(),"	small y, umlaut mark");
        htmlEntities.put("&forall;".toLowerCase(),"	for all");
        htmlEntities.put("&part;".toLowerCase(),"	part");
        htmlEntities.put("&exist;".toLowerCase(),"	exists");
        htmlEntities.put("&empty;".toLowerCase(),"	empty");
        htmlEntities.put("&nabla;".toLowerCase(),"	nabla");
        htmlEntities.put("&isin;".toLowerCase(),"	isin");
        htmlEntities.put("&notin;".toLowerCase(),"	notin");
        htmlEntities.put("&ni;".toLowerCase(),"	ni");
        htmlEntities.put("&prod;".toLowerCase(),"	prod");
        htmlEntities.put("&sum;".toLowerCase(),"	sum");
        htmlEntities.put("&minus;".toLowerCase(),"	minus");
        htmlEntities.put("&lowast;".toLowerCase(),"	lowast");
        htmlEntities.put("&radic;".toLowerCase(),"	square root");
        htmlEntities.put("&prop;".toLowerCase(),"	proportional to");
        htmlEntities.put("&infin;".toLowerCase(),"	infinity");
        htmlEntities.put("&ang;".toLowerCase(),"	angle");
        htmlEntities.put("&and;".toLowerCase(),"	and");
        htmlEntities.put("&or;".toLowerCase(),"	or");
        htmlEntities.put("&cap;".toLowerCase(),"	cap");
        htmlEntities.put("&cup;".toLowerCase(),"	cup");
        htmlEntities.put("&int;".toLowerCase(),"	integral");
        htmlEntities.put("&there4;".toLowerCase(),"	therefore");
        htmlEntities.put("&sim;".toLowerCase(),"	similar to");
        htmlEntities.put("&cong;".toLowerCase(),"	congruent to");
        htmlEntities.put("&asymp;".toLowerCase(),"	almost equal");
        htmlEntities.put("&ne;".toLowerCase(),"	not equal");
        htmlEntities.put("&equiv;".toLowerCase(),"	equivalent");
        htmlEntities.put("&le;".toLowerCase(),"	less or equal");
        htmlEntities.put("&ge;".toLowerCase(),"	greater or equal");
        htmlEntities.put("&sub;".toLowerCase(),"	subset of");
        htmlEntities.put("&sup;".toLowerCase(),"	superset of");
        htmlEntities.put("&nsub;".toLowerCase(),"	not subset of");
        htmlEntities.put("&sube;".toLowerCase(),"	subset or equal");
        htmlEntities.put("&supe;".toLowerCase(),"	superset or equal");
        htmlEntities.put("&oplus;".toLowerCase(),"	circled plus");
        htmlEntities.put("&otimes;".toLowerCase(),"	cirled times");
        htmlEntities.put("&perp;".toLowerCase(),"	perpendicular");
        htmlEntities.put("&sdot;".toLowerCase(),"	dot operator");
        htmlEntities.put("&Alpha;".toLowerCase(),"	Alpha");
        htmlEntities.put("&Beta;".toLowerCase(),"	Beta");
        htmlEntities.put("&Gamma;".toLowerCase(),"	Gamma");
        htmlEntities.put("&Delta;".toLowerCase(),"	Delta");
        htmlEntities.put("&Epsilon;".toLowerCase(),"	Epsilon");
        htmlEntities.put("&Zeta;".toLowerCase(),"	Zeta");
        htmlEntities.put("&Eta;".toLowerCase(),"	Eta");
        htmlEntities.put("&Theta;".toLowerCase(),"	Theta");
        htmlEntities.put("&Iota;".toLowerCase(),"	Iota");
        htmlEntities.put("&Kappa;".toLowerCase(),"	Kappa");
        htmlEntities.put("&Lambda;".toLowerCase(),"	Lambda");
        htmlEntities.put("&Mu;".toLowerCase(),"	Mu");
        htmlEntities.put("&Nu;".toLowerCase(),"	Nu");
        htmlEntities.put("&Xi;".toLowerCase(),"	Xi");
        htmlEntities.put("&Omicron;".toLowerCase(),"	Omicron");
        htmlEntities.put("&Pi;".toLowerCase(),"	Pi");
        htmlEntities.put("&Rho;".toLowerCase(),"	Rho");
        htmlEntities.put("&Sigma;".toLowerCase(),"	Sigma");
        htmlEntities.put("&Tau;".toLowerCase(),"	Tau");
        htmlEntities.put("&Upsilon;".toLowerCase(),"	Upsilon");
        htmlEntities.put("&Phi;".toLowerCase(),"	Phi");
        htmlEntities.put("&Chi;".toLowerCase(),"	Chi");
        htmlEntities.put("&Psi;".toLowerCase(),"	Psi");
        htmlEntities.put("&Omega;".toLowerCase(),"	Omega");
        htmlEntities.put("&alpha;".toLowerCase(),"	alpha");
        htmlEntities.put("&beta;".toLowerCase(),"	beta");
        htmlEntities.put("&gamma;".toLowerCase(),"	gamma");
        htmlEntities.put("&delta;".toLowerCase(),"	delta");
        htmlEntities.put("&epsilon;".toLowerCase(),"	epsilon");
        htmlEntities.put("&zeta;".toLowerCase(),"	zeta");
        htmlEntities.put("&eta;".toLowerCase(),"	eta");
        htmlEntities.put("&theta;".toLowerCase(),"	theta");
        htmlEntities.put("&iota;".toLowerCase(),"	iota");
        htmlEntities.put("&kappa;".toLowerCase(),"	kappa");
        htmlEntities.put("&lambda;".toLowerCase(),"	lambda");
        htmlEntities.put("&mu;".toLowerCase(),"	mu");
        htmlEntities.put("&nu;".toLowerCase(),"	nu");
        htmlEntities.put("&xi;".toLowerCase(),"	xi");
        htmlEntities.put("&omicron;".toLowerCase(),"	omicron");
        htmlEntities.put("&pi;".toLowerCase(),"	pi");
        htmlEntities.put("&rho;".toLowerCase(),"	rho");
        htmlEntities.put("&sigmaf;".toLowerCase(),"	sigmaf");
        htmlEntities.put("&sigma;".toLowerCase(),"	sigma");
        htmlEntities.put("&tau;".toLowerCase(),"	tau");
        htmlEntities.put("&upsilon;".toLowerCase(),"	upsilon");
        htmlEntities.put("&phi;".toLowerCase(),"	phi");
        htmlEntities.put("&chi;".toLowerCase(),"	chi");
        htmlEntities.put("&psi;".toLowerCase(),"	psi");
        htmlEntities.put("&omega;".toLowerCase(),"	omega");
        htmlEntities.put("&thetasym;".toLowerCase(),"	theta symbol");
        htmlEntities.put("&upsih;".toLowerCase(),"	upsilon symbol");
        htmlEntities.put("&piv;".toLowerCase(),"	pi symbol");
        htmlEntities.put("&OElig;".toLowerCase(),"	capital ligature OE");
        htmlEntities.put("&oelig;".toLowerCase(),"	small ligature oe");
        htmlEntities.put("&Scaron;".toLowerCase(),"	capital S with caron");
        htmlEntities.put("&scaron;".toLowerCase(),"	small S with caron");
        htmlEntities.put("&Yuml;".toLowerCase(),"	capital Y with diaeres");
        htmlEntities.put("&fnof;".toLowerCase(),"	f with hook");
        htmlEntities.put("&circ;".toLowerCase(),"	modifier letter circumflex accent");
        htmlEntities.put("&tilde;".toLowerCase(),"	small tilde");
        htmlEntities.put("&ensp;".toLowerCase(),"	en space");
        htmlEntities.put("&emsp;".toLowerCase(),"	em space");
        htmlEntities.put("&thinsp;".toLowerCase(),"	thin space");
        htmlEntities.put("&zwnj;".toLowerCase(),"	zero width non-joiner");
        htmlEntities.put("&zwj;".toLowerCase(),"	zero width joiner");
        htmlEntities.put("&lrm;".toLowerCase(),"	left-to-right mark");
        htmlEntities.put("&rlm;".toLowerCase(),"	right-to-left mark");
        htmlEntities.put("&ndash;".toLowerCase(),"	en dash");
        htmlEntities.put("&mdash;".toLowerCase(),"	em dash");
        htmlEntities.put("&lsquo;".toLowerCase(),"	left single quotation mark");
        htmlEntities.put("&rsquo;".toLowerCase(),"	right single quotation mark");
        htmlEntities.put("&sbquo;".toLowerCase(),"	single low-9 quotation mark");
        htmlEntities.put("&ldquo;".toLowerCase(),"	left double quotation mark");
        htmlEntities.put("&rdquo;".toLowerCase(),"	right double quotation mark");
        htmlEntities.put("&bdquo;".toLowerCase(),"	double low-9 quotation mark");
        htmlEntities.put("&dagger;".toLowerCase(),"	dagger");
        htmlEntities.put("&Dagger;".toLowerCase(),"	double dagger");
        htmlEntities.put("&bull;".toLowerCase(),"	bullet");
        htmlEntities.put("&hellip;".toLowerCase(),"	horizontal ellipsis");
        htmlEntities.put("&permil;".toLowerCase(),"	per mille ");
        htmlEntities.put("&prime;".toLowerCase(),"	minutes");
        htmlEntities.put("&Prime;".toLowerCase(),"	seconds");
        htmlEntities.put("&lsaquo;".toLowerCase(),"	single left angle quotation");
        htmlEntities.put("&rsaquo;".toLowerCase(),"	single right angle quotation");
        htmlEntities.put("&oline;".toLowerCase(),"	overline");
        htmlEntities.put("&euro;".toLowerCase(),"	euro");
        htmlEntities.put("&trade;".toLowerCase(),"	trademark");
        htmlEntities.put("&larr;".toLowerCase(),"	left arrow");
        htmlEntities.put("&uarr;".toLowerCase(),"	up arrow");
        htmlEntities.put("&rarr;".toLowerCase(),"	right arrow");
        htmlEntities.put("&darr;".toLowerCase(),"	down arrow");
        htmlEntities.put("&harr;".toLowerCase(),"	left right arrow");
        htmlEntities.put("&crarr;".toLowerCase(),"	carriage return arrow");
        htmlEntities.put("&lceil;".toLowerCase(),"	left ceiling");
        htmlEntities.put("&rceil;".toLowerCase(),"	right ceiling");
        htmlEntities.put("&lfloor;".toLowerCase(),"	left floor");
        htmlEntities.put("&rfloor;".toLowerCase(),"	right floor");
        htmlEntities.put("&loz;".toLowerCase(),"	lozenge");
        htmlEntities.put("&spades;".toLowerCase(),"	spade");
        htmlEntities.put("&clubs;".toLowerCase(),"	club");
        htmlEntities.put("&hearts;".toLowerCase(),"	heart");
        htmlEntities.put("&diams;".toLowerCase(),"	diamond");
        
    	
        int max = 0;
        for (String keys : htmlEntities.keySet()) {
            if(max < keys.length()) max = keys.length();
        }
        maxLength = max;
        
        */
    	
    	int max = 0;
        for (String keys : HtmlEntries.entry_set) {
            if(max < keys.length()) max = keys.length();
        }
        maxLength = max;
    }

    /**
     * This is to escape "&" into "&amp;" if it is not already escaped.
     * The reason is that Tagsoup library will encounter a PushBack buffer overflow
     * exception if it encounter unescaped "&".
     */
    public static String escapeAmp(String source) {
        List<Integer> toReplaceIndex = new ArrayList<Integer>();
        int cur = 0;
        int j=0;
        while(true) {
            int i = source.indexOf('&', cur);
            if(i==-1) break; // not found
            if(j!=-1&&j<=i) j = source.indexOf(';',i+1);

            if(j==-1) { // not found ';', replace with &amp;
                toReplaceIndex.add(i);
                cur = i+1;
            } else {
                int to = Math.min(j+1, i + maxLength);
                String entityToLookFor = source.substring(i, to);
                
                //防类似&#38被写&#038或&#0038，特去掉中间的0
                if(entityToLookFor.matches("&#\\d+;")) 
                	entityToLookFor="&#"+Integer.parseInt(entityToLookFor.substring(2, entityToLookFor.length()-1))+";";
                
                if(HtmlEntries.entry_set.contains(StringUtils.lowerCase(entityToLookFor))) { // is proper entity, ignore
                    cur = i+1;
                } else {    // replace
                    toReplaceIndex.add(i);
                    cur = i+1;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        cur = 0;
        for (Integer i : toReplaceIndex) {
            sb.append(source.substring(cur, i)).append("&amp;");
            cur = i+1;
        }
        sb.append(source.substring(cur));
        return sb.toString();
    }

    public static String convert(String src) throws ExtractException {
		try {
			XMLReader parser = new Parser();
			HTMLSchema schema = new HTMLSchema();
			parser.setProperty(Parser.schemaProperty, schema);

			StringWriter writer = new StringWriter();
			XMLWriter handler = new XMLWriter(writer);
			parser.setContentHandler(handler);

			InputSource source = new InputSource();
			source.setEncoding("UTF-8");
			source.setByteStream(IOUtils.toInputStream(escapeAmp(src), "UTF-8"));
			
			parser.parse(source);

			return writer.toString();
		} catch(Exception e) {
		    throw new ExtractException("Fail to convert html to xml",e);
		}
	}
}
