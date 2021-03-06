package inveno.spider.parser.base;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("unchecked")
public class HtmlEntries {
//	private static final String[] htmlEntries=new String[]{
//	  	"?	&#32; 	space".toLowerCase(),
//	  	"! 	&#33; 	exclamation mark".toLowerCase(),
//	  	"\" &#34; 	quotation mark".toLowerCase(),
//	  	"# 	&#35; 	number sign".toLowerCase(),
//	  	"$ 	&#36; 	dollar sign".toLowerCase(),
//	  	"% 	&#37; 	percent sign".toLowerCase(),
//	  	"& 	&#38; 	ampersand".toLowerCase(),
//	  	"' 	&#39; 	apostrophe".toLowerCase(),
//	  	"( 	&#40; 	left parenthesis".toLowerCase(),
//	  	") 	&#41; 	right parenthesis".toLowerCase(),
//	  	"* 	&#42; 	asterisk".toLowerCase(),
//	  	"+ 	&#43; 	plus sign".toLowerCase(),
//	  	", 	&#44; 	comma".toLowerCase(),
//	  	"- 	&#45; 	hyphen".toLowerCase(),
//	  	". 	&#46; 	period".toLowerCase(),
//	  	"/ 	&#47; 	slash".toLowerCase(),
//	  	"0 	&#48; 	digit 0".toLowerCase(),
//	  	"1 	&#49; 	digit 1".toLowerCase(),
//	  	"2 	&#50; 	digit 2".toLowerCase(),
//	  	"3 	&#51; 	digit 3".toLowerCase(),
//	  	"4 	&#52; 	digit 4".toLowerCase(),
//	  	"5 	&#53; 	digit 5".toLowerCase(),
//	  	"6 	&#54; 	digit 6".toLowerCase(),
//	  	"7 	&#55; 	digit 7".toLowerCase(),
//	  	"8 	&#56; 	digit 8".toLowerCase(),
//	  	"9 	&#57; 	digit 9".toLowerCase(),
//	  	": 	&#58; 	colon".toLowerCase(),
//	  	"; 	&#59; 	semicolon".toLowerCase(),
//	  	"< 	&#60; 	less-than".toLowerCase(),
//	  	"= 	&#61; 	equals-to".toLowerCase(),
//	  	"> 	&#62; 	greater-than".toLowerCase(),
//	  	"? 	&#63; 	question mark".toLowerCase(),
//	  	"@ 	&#64; 	at sign".toLowerCase(),
//	  	"A 	&#65; 	uppercase A".toLowerCase(),
//	  	"B 	&#66; 	uppercase B".toLowerCase(),
//	  	"C 	&#67; 	uppercase C".toLowerCase(),
//	  	"D 	&#68; 	uppercase D".toLowerCase(),
//	  	"E 	&#69; 	uppercase E".toLowerCase(),
//	  	"F 	&#70; 	uppercase F".toLowerCase(),
//	  	"G 	&#71; 	uppercase G".toLowerCase(),
//	  	"H 	&#72; 	uppercase H".toLowerCase(),
//	  	"I 	&#73; 	uppercase I".toLowerCase(),
//	  	"J 	&#74; 	uppercase J".toLowerCase(),
//	  	"K 	&#75; 	uppercase K".toLowerCase(),
//	  	"L 	&#76; 	uppercase L".toLowerCase(),
//	  	"M 	&#77; 	uppercase M".toLowerCase(),
//	  	"N 	&#78; 	uppercase N".toLowerCase(),
//	  	"O 	&#79; 	uppercase O".toLowerCase(),
//	  	"P 	&#80; 	uppercase P".toLowerCase(),
//	  	"Q 	&#81; 	uppercase Q".toLowerCase(),
//	  	"R 	&#82; 	uppercase R".toLowerCase(),
//	  	"S 	&#83; 	uppercase S".toLowerCase(),
//	  	"T 	&#84; 	uppercase T".toLowerCase(),
//	  	"U 	&#85; 	uppercase U".toLowerCase(),
//	  	"V 	&#86; 	uppercase V".toLowerCase(),
//	  	"W 	&#87; 	uppercase W".toLowerCase(),
//	  	"X 	&#88; 	uppercase X".toLowerCase(),
//	  	"Y 	&#89; 	uppercase Y".toLowerCase(),
//	  	"Z 	&#90; 	uppercase Z".toLowerCase(),
//	  	"[ 	&#91; 	left square bracket".toLowerCase(),
//	  	"\\ &#92; 	backslash".toLowerCase(),
//	  	"] 	&#93; 	right square bracket".toLowerCase(),
//	  	"^ 	&#94; 	caret".toLowerCase(),
//	  	"_ 	&#95; 	underscore".toLowerCase(),
//	  	"` 	&#96; 	grave accent".toLowerCase(),
//	  	"a 	&#97; 	lowercase a".toLowerCase(),
//	  	"b 	&#98; 	lowercase b".toLowerCase(),
//	  	"c 	&#99; 	lowercase c".toLowerCase(),
//	  	"d 	&#100; 	lowercase d".toLowerCase(),
//	  	"e 	&#101; 	lowercase e".toLowerCase(),
//	  	"f 	&#102; 	lowercase f".toLowerCase(),
//	  	"g 	&#103; 	lowercase g".toLowerCase(),
//	  	"h 	&#104; 	lowercase h".toLowerCase(),
//	  	"i 	&#105; 	lowercase i".toLowerCase(),
//	  	"j 	&#106; 	lowercase j".toLowerCase(),
//	  	"k 	&#107; 	lowercase k".toLowerCase(),
//	  	"l 	&#108; 	lowercase l".toLowerCase(),
//	  	"m 	&#109; 	lowercase m".toLowerCase(),
//	  	"n 	&#110; 	lowercase n".toLowerCase(),
//	  	"o 	&#111; 	lowercase o".toLowerCase(),
//	  	"p 	&#112; 	lowercase p".toLowerCase(),
//	  	"q 	&#113; 	lowercase q".toLowerCase(),
//	  	"r 	&#114; 	lowercase r".toLowerCase(),
//	  	"s 	&#115; 	lowercase s".toLowerCase(),
//	  	"t 	&#116; 	lowercase t".toLowerCase(),
//	  	"u 	&#117; 	lowercase u".toLowerCase(),
//	  	"v 	&#118; 	lowercase v".toLowerCase(),
//	  	"w 	&#119; 	lowercase w".toLowerCase(),
//	  	"x 	&#120; 	lowercase x".toLowerCase(),
//	  	"y 	&#121; 	lowercase y".toLowerCase(),
//	  	"z 	&#122; 	lowercase z".toLowerCase(),
//	  	"{ 	&#123; 	left curly brace".toLowerCase(),
//	  	"| 	&#124; 	vertical bar".toLowerCase(),
//	  	"} 	&#125; 	right curly brace".toLowerCase(),
//	  	"~ 	&#126; 	tilde".toLowerCase(),
//	  	"\" &#34; 	&quot; 	quotation mark".toLowerCase(),
//	  	"' 	&#39; 	&apos; (does not work in IE) 	apostrophe".toLowerCase(), 
//	  	"& 	&#38; 	&amp; 	ampersand".toLowerCase(),
//	  	"< 	&#60; 	&lt; 	less-than".toLowerCase(),
//	  	"> 	&#62; 	&gt; 	greater-than".toLowerCase(),
//	  	"?	&#160; 	&nbsp; 	non-breaking space".toLowerCase(),
//	  	"¡ 	&#161; 	&iexcl; 	inverted exclamation mark".toLowerCase(),
//	  	"¢ 	&#162; 	&cent; 	cent".toLowerCase(),
//	  	"£ 	&#163; 	&pound; 	pound".toLowerCase(),
//	  	"¤ 	&#164; 	&curren; 	currency".toLowerCase(),
//	  	"¥ 	&#165; 	&yen; 	yen".toLowerCase(),
//	  	"¦ 	&#166; 	&brvbar; 	broken vertical bar".toLowerCase(),
//	  	"§ 	&#167; 	&sect; 	section".toLowerCase(),
//	  	"¨ 	&#168; 	&uml; 	spacing diaeresis".toLowerCase(),
//	  	"© 	&#169; 	&copy; 	copyright".toLowerCase(),
//	  	"ª 	&#170; 	&ordf; 	feminine ordinal indicator".toLowerCase(),
//	  	"« 	&#171; 	&laquo; 	angle quotation mark (left)".toLowerCase(),
//	  	"¬ 	&#172; 	&not; 	negation".toLowerCase(),
//	  	"­ 	&#173; 	&shy; 	soft hyphen".toLowerCase(),
//	  	"® 	&#174; 	&reg; 	registered trademark".toLowerCase(),
//	  	"¯ 	&#175; 	&macr; 	spacing macron".toLowerCase(),
//	  	"° 	&#176; 	&deg; 	degree".toLowerCase(),
//	  	"± 	&#177; 	&plusmn; 	plus-or-minus".toLowerCase(), 
//	  	"² 	&#178; 	&sup2; 	superscript 2".toLowerCase(),
//	  	"³ 	&#179; 	&sup3; 	superscript 3".toLowerCase(),
//	  	"´ 	&#180; 	&acute; 	spacing acute".toLowerCase(),
//	  	"µ 	&#181; 	&micro; 	micro".toLowerCase(),
//	  	"¶ 	&#182; 	&para; 	paragraph".toLowerCase(),
//	  	"· 	&#183; 	&middot; 	middle dot".toLowerCase(),
//	  	"¸ 	&#184; 	&cedil; 	spacing cedilla".toLowerCase(),
//	  	"¹ 	&#185; 	&sup1; 	superscript 1".toLowerCase(),
//	  	"º 	&#186; 	&ordm; 	masculine ordinal indicator".toLowerCase(),
//	  	"» 	&#187; 	&raquo; 	angle quotation mark (right)".toLowerCase(),
//	  	"¼ 	&#188; 	&frac14; 	fraction 1/4".toLowerCase(),
//	  	"½ 	&#189; 	&frac12; 	fraction 1/2".toLowerCase(),
//	  	"¾ 	&#190; 	&frac34; 	fraction 3/4".toLowerCase(),
//	  	"¿ 	&#191; 	&iquest; 	inverted question mark".toLowerCase(),
//	  	"× 	&#215; 	&times; 	multiplication".toLowerCase(),
//	  	"÷ 	&#247; 	&divide; 	division".toLowerCase(),
//	  	"À 	&#192; 	&Agrave; 	capital a, grave accent".toLowerCase(),
//	  	"Á 	&#193; 	&Aacute; 	capital a, acute accent".toLowerCase(),
//	  	"Â 	&#194; 	&Acirc; 	capital a, circumflex accent".toLowerCase(),
//	  	"Ã 	&#195; 	&Atilde; 	capital a, tilde".toLowerCase(),
//	  	"Ä 	&#196; 	&Auml; 	capital a, umlaut mark".toLowerCase(),
//	  	"Å 	&#197; 	&Aring; 	capital a, ring".toLowerCase(),
//	  	"Æ 	&#198; 	&AElig; 	capital ae".toLowerCase(),
//	  	"Ç 	&#199; 	&Ccedil; 	capital c, cedilla".toLowerCase(),
//	  	"È 	&#200; 	&Egrave; 	capital e, grave accent".toLowerCase(),
//	  	"É 	&#201; 	&Eacute; 	capital e, acute accent".toLowerCase(),
//	  	"Ê 	&#202; 	&Ecirc; 	capital e, circumflex accent".toLowerCase(),
//	  	"Ë 	&#203; 	&Euml; 	capital e, umlaut mark".toLowerCase(),
//	  	"Ì 	&#204; 	&Igrave; 	capital i, grave accent".toLowerCase(),
//	  	"Í 	&#205; 	&Iacute; 	capital i, acute accent".toLowerCase(),
//	  	"Î 	&#206; 	&Icirc; 	capital i, circumflex accent".toLowerCase(),
//	  	"Ï 	&#207; 	&Iuml; 	capital i, umlaut mark".toLowerCase(),
//	  	"Ð 	&#208; 	&ETH; 	capital eth, Icelandic".toLowerCase(),
//	  	"Ñ 	&#209; 	&Ntilde; 	capital n, tilde".toLowerCase(),
//	  	"Ò 	&#210; 	&Ograve; 	capital o, grave accent".toLowerCase(),
//	  	"Ó 	&#211; 	&Oacute; 	capital o, acute accent".toLowerCase(),
//	  	"Ô 	&#212; 	&Ocirc; 	capital o, circumflex accent".toLowerCase(),
//	  	"Õ 	&#213; 	&Otilde; 	capital o, tilde".toLowerCase(),
//	  	"Ö 	&#214; 	&Ouml; 	capital o, umlaut mark".toLowerCase(),
//	  	"Ø 	&#216; 	&Oslash; 	capital o, slash".toLowerCase(),
//	  	"Ù 	&#217; 	&Ugrave; 	capital u, grave accent".toLowerCase(),
//	  	"Ú 	&#218; 	&Uacute; 	capital u, acute accent".toLowerCase(),
//	  	"Û 	&#219; 	&Ucirc; 	capital u, circumflex accent".toLowerCase(),
//	  	"Ü 	&#220; 	&Uuml; 	capital u, umlaut mark".toLowerCase(),
//	  	"Ý 	&#221; 	&Yacute; 	capital y, acute accent".toLowerCase(),
//	  	"Þ 	&#222; 	&THORN; 	capital THORN, Icelandic".toLowerCase(),
//	  	"ß 	&#223; 	&szlig; 	small sharp s, German".toLowerCase(),
//	  	"à 	&#224; 	&agrave; 	small a, grave accent".toLowerCase(),
//	  	"á 	&#225; 	&aacute; 	small a, acute accent".toLowerCase(),
//	  	"â 	&#226; 	&acirc; 	small a, circumflex accent".toLowerCase(),
//	  	"ã 	&#227; 	&atilde; 	small a, tilde".toLowerCase(),
//	  	"ä 	&#228; 	&auml; 	small a, umlaut mark".toLowerCase(),
//	  	"å 	&#229; 	&aring; 	small a, ring".toLowerCase(),
//	  	"æ 	&#230; 	&aelig; 	small ae".toLowerCase(),
//	  	"ç 	&#231; 	&ccedil; 	small c, cedilla".toLowerCase(),
//	  	"è 	&#232; 	&egrave; 	small e, grave accent".toLowerCase(),
//	  	"é 	&#233; 	&eacute; 	small e, acute accent".toLowerCase(),
//	  	"ê 	&#234; 	&ecirc; 	small e, circumflex accent".toLowerCase(),
//	  	"ë 	&#235; 	&euml; 	small e, umlaut mark".toLowerCase(),
//	  	"ì 	&#236; 	&igrave; 	small i, grave accent".toLowerCase(),
//	  	"í 	&#237; 	&iacute; 	small i, acute accent".toLowerCase(),
//	  	"î 	&#238; 	&icirc; 	small i, circumflex accent".toLowerCase(),
//	  	"ï 	&#239; 	&iuml; 	small i, umlaut mark".toLowerCase(),
//	  	"ð 	&#240; 	&eth; 	small eth, Icelandic".toLowerCase(),
//	  	"ñ 	&#241; 	&ntilde; 	small n, tilde".toLowerCase(),
//	  	"ò 	&#242; 	&ograve; 	small o, grave accent".toLowerCase(),
//	  	"ó 	&#243; 	&oacute; 	small o, acute accent".toLowerCase(),
//	  	"ô 	&#244; 	&ocirc; 	small o, circumflex accent".toLowerCase(),
//	  	"õ 	&#245; 	&otilde; 	small o, tilde".toLowerCase(),
//	  	"ö 	&#246; 	&ouml; 	small o, umlaut mark".toLowerCase(),
//	  	"ø 	&#248; 	&oslash; 	small o, slash".toLowerCase(),
//	  	"ù 	&#249; 	&ugrave; 	small u, grave accent".toLowerCase(),
//	  	"ú 	&#250; 	&uacute; 	small u, acute accent".toLowerCase(),
//	  	"û 	&#251; 	&ucirc; 	small u, circumflex accent".toLowerCase(),
//	  	"ü 	&#252; 	&uuml; 	small u, umlaut mark".toLowerCase(),
//	  	"ý 	&#253; 	&yacute; 	small y, acute accent".toLowerCase(),
//	  	"þ 	&#254; 	&thorn; 	small thorn, Icelandic".toLowerCase(),
//	  	"ÿ 	&#255; 	&yuml; 	small y, umlaut mark".toLowerCase(),
//	  	"∀ 	&#8704; 	&forall; 	for all".toLowerCase(),
//	  	"∂ 	&#8706; 	&part; 	part".toLowerCase(),
//	  	"∃ 	&#8707; 	&exist; 	exists".toLowerCase(),
//	  	"∅ 	&#8709; 	&empty; 	empty".toLowerCase(),
//	  	"∇ 	&#8711; 	&nabla; 	nabla".toLowerCase(),
//	  	"∈ 	&#8712; 	&isin; 	isin".toLowerCase(),
//	  	"∉ 	&#8713; 	&notin; 	notin".toLowerCase(),
//	  	"∋ 	&#8715; 	&ni; 	ni".toLowerCase(),
//	  	"∏ 	&#8719; 	&prod; 	prod".toLowerCase(),
//	  	"∑ 	&#8721; 	&sum; 	sum".toLowerCase(),
//	  	"− 	&#8722; 	&minus; 	minus".toLowerCase(),
//	  	"∗ 	&#8727; 	&lowast; 	lowast".toLowerCase(),
//	  	"√ 	&#8730; 	&radic; 	square root".toLowerCase(),
//	  	"∝ 	&#8733; 	&prop; 	proportional to".toLowerCase(),
//	  	"∞ 	&#8734; 	&infin; 	infinity".toLowerCase(),
//	  	"∠ 	&#8736; 	&ang; 	angle".toLowerCase(),
//	  	"∧ 	&#8743; 	&and; 	and".toLowerCase(),
//	  	"∨ 	&#8744; 	&or; 	or".toLowerCase(),
//	  	"∩ 	&#8745; 	&cap; 	cap".toLowerCase(),
//	  	"∪ 	&#8746; 	&cup; 	cup".toLowerCase(),
//	  	"∫ 	&#8747; 	&int; 	integral".toLowerCase(),
//	  	"∴ 	&#8756; 	&there4; 	therefore".toLowerCase(),
//	  	"∼ 	&#8764; 	&sim; 	similar to".toLowerCase(),
//	  	"≅ 	&#8773; 	&cong; 	congruent to".toLowerCase(),
//	  	"≈ 	&#8776; 	&asymp; 	almost equal".toLowerCase(),
//	  	"≠ 	&#8800; 	&ne; 	not equal".toLowerCase(),
//	  	"≡ 	&#8801; 	&equiv; 	equivalent".toLowerCase(),
//	  	"≤ 	&#8804; 	&le; 	less or equal".toLowerCase(),
//	  	"≥ 	&#8805; 	&ge; 	greater or equal".toLowerCase(),
//	  	"⊂ 	&#8834; 	&sub; 	subset of".toLowerCase(),
//	  	"⊃ 	&#8835; 	&sup; 	superset of".toLowerCase(),
//	  	"⊄ 	&#8836; 	&nsub; 	not subset of".toLowerCase(),
//	  	"⊆ 	&#8838; 	&sube; 	subset or equal".toLowerCase(),
//	  	"⊇ 	&#8839; 	&supe; 	superset or equal".toLowerCase(),
//	  	"⊕ 	&#8853; 	&oplus; 	circled plus".toLowerCase(),
//	  	"⊗ 	&#8855; 	&otimes; 	cirled times".toLowerCase(),
//	  	"⊥ 	&#8869; 	&perp; 	perpendicular".toLowerCase(),
//	  	"⋅ 	&#8901; 	&sdot; 	dot operator".toLowerCase(),
//	  	"Α 	&#913; 	&Alpha; 	Alpha".toLowerCase(),
//	  	"Β 	&#914; 	&Beta; 	Beta".toLowerCase(),
//	  	"Γ 	&#915; 	&Gamma; 	Gamma".toLowerCase(),
//	  	"Δ 	&#916; 	&Delta; 	Delta".toLowerCase(),
//	  	"Ε 	&#917; 	&Epsilon; 	Epsilon".toLowerCase(),
//	  	"Ζ 	&#918; 	&Zeta; 	Zeta".toLowerCase(),
//	  	"Η 	&#919; 	&Eta; 	Eta".toLowerCase(),
//	  	"Θ 	&#920; 	&Theta; 	Theta".toLowerCase(),
//	  	"Ι 	&#921; 	&Iota; 	Iota".toLowerCase(),
//	  	"Κ 	&#922; 	&Kappa; 	Kappa".toLowerCase(),
//	  	"Λ 	&#923; 	&Lambda; 	Lambda".toLowerCase(),
//	  	"Μ 	&#924; 	&Mu; 	Mu".toLowerCase(),
//	  	"Ν 	&#925; 	&Nu; 	Nu".toLowerCase(),
//	  	"Ξ 	&#926; 	&Xi; 	Xi".toLowerCase(),
//	  	"Ο 	&#927; 	&Omicron; 	Omicron".toLowerCase(),
//	  	"Π 	&#928; 	&Pi; 	Pi".toLowerCase(),
//	  	"Ρ 	&#929; 	&Rho; 	Rho".toLowerCase(),
//	  	"Σ 	&#931; 	&Sigma; 	Sigma".toLowerCase(),
//	  	"Τ 	&#932; 	&Tau; 	Tau".toLowerCase(),
//	  	"Υ 	&#933; 	&Upsilon; 	Upsilon".toLowerCase(),
//	  	"Φ 	&#934; 	&Phi; 	Phi".toLowerCase(),
//	  	"Χ 	&#935; 	&Chi; 	Chi".toLowerCase(),
//	  	"Ψ 	&#936; 	&Psi; 	Psi".toLowerCase(),
//	  	"Ω 	&#937; 	&Omega; 	Omega".toLowerCase(),
//	  	"α 	&#945; 	&alpha; 	alpha".toLowerCase(),
//	  	"β 	&#946; 	&beta; 	beta".toLowerCase(),
//	  	"γ 	&#947; 	&gamma; 	gamma".toLowerCase(),
//	  	"δ 	&#948; 	&delta; 	delta".toLowerCase(),
//	  	"ε 	&#949; 	&epsilon; 	epsilon".toLowerCase(),
//	  	"ζ 	&#950; 	&zeta; 	zeta".toLowerCase(),
//	  	"η 	&#951; 	&eta; 	eta".toLowerCase(),
//	  	"θ 	&#952; 	&theta; 	theta".toLowerCase(),
//	  	"ι 	&#953; 	&iota; 	iota".toLowerCase(),
//	  	"κ 	&#954; 	&kappa; 	kappa".toLowerCase(),
//	  	"λ 	&#955; 	&lambda; 	lambda".toLowerCase(),
//	  	"μ 	&#956; 	&mu; 	mu".toLowerCase(),
//	  	"ν 	&#957; 	&nu; 	nu".toLowerCase(),
//	  	"ξ 	&#958; 	&xi; 	xi".toLowerCase(),
//	  	"ο 	&#959; 	&omicron; 	omicron".toLowerCase(),
//	  	"π 	&#960; 	&pi; 	pi".toLowerCase(),
//	  	"ρ 	&#961; 	&rho; 	rho".toLowerCase(),
//	  	"ς 	&#962; 	&sigmaf; 	sigmaf".toLowerCase(),
//	  	"σ 	&#963; 	&sigma; 	sigma".toLowerCase(),
//	  	"τ 	&#964; 	&tau; 	tau".toLowerCase(),
//	  	"υ 	&#965; 	&upsilon; 	upsilon".toLowerCase(),
//	  	"φ 	&#966; 	&phi; 	phi".toLowerCase(),
//	  	"χ 	&#967; 	&chi; 	chi".toLowerCase(),
//	  	"ψ 	&#968; 	&psi; 	psi".toLowerCase(),
//	  	"ω 	&#969; 	&omega; 	omega".toLowerCase(),
//	  	"ϑ 	&#977; 	&thetasym; 	theta symbol".toLowerCase(),
//	  	"ϒ 	&#978; 	&upsih; 	upsilon symbol".toLowerCase(),
//	  	"ϖ 	&#982; 	&piv; 	pi symbol".toLowerCase(),
//	  	"Œ 	&#338; 	&OElig; 	capital ligature OE".toLowerCase(),
//	  	"œ 	&#339; 	&oelig; 	small ligature oe".toLowerCase(),
//	  	"Š 	&#352; 	&Scaron; 	capital S with caron".toLowerCase(),
//	  	"š 	&#353; 	&scaron; 	small S with caron".toLowerCase(),
//	  	"Ÿ 	&#376; 	&Yuml; 	capital Y with diaeres".toLowerCase(),
//	  	"ƒ 	&#402; 	&fnof; 	f with hook".toLowerCase(),
//	  	"ˆ 	&#710; 	&circ; 	modifier letter circumflex accent".toLowerCase(),
//	  	"˜ 	&#732; 	&tilde; 	small tilde".toLowerCase(),
//	  	"?  &#8194; 	&ensp; 	en space".toLowerCase(),
//	  	"?  &#8195; 	&emsp; 	em space".toLowerCase(),
//	  	"?  &#8201; 	&thinsp; 	thin space".toLowerCase(),
//	  	"? 	&#8204; 	&zwnj; 	zero width non-joiner".toLowerCase(),
//	  	"‍? 	&#8205; 	&zwj; 	zero width joiner".toLowerCase(),
//	  	"‎‍? 	&#8206; 	&lrm; 	left-to-right mark".toLowerCase(),
//	  	"‍?‏ 	&#8207; 	&rlm; 	right-to-left mark".toLowerCase(),
//	  	"– 	&#8211; 	&ndash; 	en dash".toLowerCase(),
//	  	"— 	&#8212; 	&mdash; 	em dash".toLowerCase(),
//	  	"‘ 	&#8216; 	&lsquo; 	left single quotation mark".toLowerCase(),
//	  	"’ 	&#8217; 	&rsquo; 	right single quotation mark".toLowerCase(),
//	  	"‚ 	&#8218; 	&sbquo; 	single low-9 quotation mark".toLowerCase(),
//	  	"“ 	&#8220; 	&ldquo; 	left double quotation mark".toLowerCase(),
//	  	"” 	&#8221; 	&rdquo; 	right double quotation mark".toLowerCase(),
//	  	"„ 	&#8222; 	&bdquo; 	double low-9 quotation mark".toLowerCase(),
//	  	"† 	&#8224; 	&dagger; 	dagger".toLowerCase(),
//	  	"‡ 	&#8225; 	&Dagger; 	double dagger".toLowerCase(),
//	  	"• 	&#8226; 	&bull; 	bullet".toLowerCase(),
//	  	"… 	&#8230; 	&hellip; 	horizontal ellipsis".toLowerCase(),
//	  	"‰ 	&#8240; 	&permil; 	per mille ".toLowerCase(),
//	  	"′ 	&#8242; 	&prime; 	minutes".toLowerCase(),
//	  	"″ 	&#8243; 	&Prime; 	seconds".toLowerCase(),
//	  	"‹ 	&#8249; 	&lsaquo; 	single left angle quotation".toLowerCase(),
//	  	"› 	&#8250; 	&rsaquo; 	single right angle quotation".toLowerCase(),
//	  	"‾ 	&#8254; 	&oline; 	overline".toLowerCase(),
//	  	"€ 	&#8364; 	&euro; 	euro".toLowerCase(),
//	  	"™ 	&#8482; 	&trade; 	trademark".toLowerCase(),
//	  	"← 	&#8592; 	&larr; 	left arrow".toLowerCase(),
//	  	"↑ 	&#8593; 	&uarr; 	up arrow".toLowerCase(),
//	  	"→ 	&#8594; 	&rarr; 	right arrow".toLowerCase(),
//	  	"↓ 	&#8595; 	&darr; 	down arrow".toLowerCase(),
//	  	"↔ 	&#8596; 	&harr; 	left right arrow".toLowerCase(),
//	  	"↵ 	&#8629; 	&crarr; 	carriage return arrow".toLowerCase(),
//	  	"? 	&#8968; 	&lceil; 	left ceiling".toLowerCase(),
//	  	"?	&#8969; 	&rceil; 	right ceiling".toLowerCase(),
//	  	"? 	&#8970; 	&lfloor; 	left floor".toLowerCase(),
//	  	"? 	&#8971; 	&rfloor; 	right floor".toLowerCase(),
//	  	"◊ 	&#9674; 	&loz; 	lozenge".toLowerCase(),
//	  	"♠ 	&#9824; 	&spades; 	spade".toLowerCase(),
//	  	"♣ 	&#9827; 	&clubs; 	club".toLowerCase(),
//	  	"♥ 	&#9829; 	&hearts; 	heart".toLowerCase(),
//	  	"♦ 	&#9830; 	&diams; 	diamond".toLowerCase(),
//	  	"， 	&#65292; 	&diams; 	逗号".toLowerCase(),
//	  	"～ 	&#65374; 	&diams; 	不知道".toLowerCase(),
//	  	"： 	&#65306; 	&diams; 	冒号".toLowerCase()
//	};
	
	public static final Set<String> entry_set=new HashSet<String>();
	static{
		try{
			List lines=FileUtils.readLines(new File(HtmlEntries.class.getResource("/tagsoup-characters.conf").getFile()), "UTF-8");
			//for(String s:htmlEntries){
			for(Object s:lines){
				String tokens[]=((String)s).toLowerCase().split("\\s+");
				if(tokens.length>=3){
					entry_set.add(tokens[1]);
					if(tokens[2].startsWith("&")) entry_set.add(tokens[2]);
				}
			}
		}catch(Exception ex){
			
		}
	}
}
