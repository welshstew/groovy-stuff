import de.vandermeer.asciitable.v2.RenderedTable
import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes

/**
 * Created by swinchester on 1/08/16.
 */
@Grab(group='de.vandermeer', module = 'asciitable', version = '0.2.5')

V2_AsciiTable at = new V2_AsciiTable();


at.addRule();
at.addRow("first row (col1)", "with some information (col2)");
at.addRule();
at.addRow("second row (col1)", "with some information (col2)");
at.addRule();

V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
rend.setWidth(new WidthAbsoluteEven(76));

RenderedTable rt = rend.render(at);

println rt