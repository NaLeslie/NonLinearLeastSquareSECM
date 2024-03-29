package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import structures.LSIterationData;
import structures.SECMImage;


/**
 *
 * @author Nathaniel
 */
public class Output {
    /**
     * Saves an SECM image as a csv formatted file
     * @param filepath the directory and filename for the files that is to be saved.
     * @param x_vals the x-positions of the datapoints
     * @param y_vals the y-positions of the datapoints
     * @param currents the currents at each datapoint. must be of the size [x-vals.length][y-vals.length]
     * @throws IOException 
     */
    public static void writeSECMImage(String filepath, double[] x_vals, double[] y_vals, double[][] currents) throws IOException{
        File f = new File(filepath);
        f.createNewFile();
        try (PrintWriter pw = new PrintWriter(f)){
            pw.print("#x,y,i");
            for(int x = 0; x < x_vals.length; x++){
                for(int y = 0; y < y_vals.length; y++){
                    pw.print("\n" + x_vals[x] + "," + y_vals[y] + "," + currents[x][y]);
                }
            }
        }
    }
    
    /**
     * Saves an SECM image as a csv formatted file
     * @param filepath the directory and filename for the files that is to be saved.
     * @param data the SECM image to be written to file
     * @throws IOException 
     */
    public static void writeSECMImage(String filepath, SECMImage data) throws IOException{
        double[] x_vals = data.getDataXvals();
        double[] y_vals = data.getDataYvals();
        double[][] currents = data.getDataCurrent();
        File f = new File(filepath);
        f.createNewFile();
        try (PrintWriter pw = new PrintWriter(f)){
            pw.print("#x,y,i");
            for(int x = 0; x < x_vals.length; x++){
                for(int y = 0; y < y_vals.length; y++){
                    pw.print("\n" + x_vals[x] + "," + y_vals[y] + "," + currents[x][y]);
                }
            }
        }
    }
    
    /**
     * Saves Levenberg-Marquardt iteration metadata to an xml formatted file
     * @param filepath the directory and filename for the files that is to be saved.
     * @param data an array where each element corresponds to a seperate iteration/lambda pair
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws TransformerException
     * @throws ParserConfigurationException 
     */
    public static void saveLSIterationData(String filepath, LSIterationData[] data) 
            throws  TransformerConfigurationException, FileNotFoundException, TransformerException, ParserConfigurationException{
        Document dom;
        dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        //create root
        Element root = dom.createElement("Iterations");
        for(int i = 0; i < data.length; i++){
            Element curr_iter = dom.createElement("Iteration");
            
            Element property = dom.createElement("Iteration_number");
            property.appendChild(dom.createTextNode(data[i].iteration_number + ""));
            curr_iter.appendChild(property);
            
            property = dom.createElement("Parameter_names");
            for (String parameter_name : data[i].parameter_names) {
                property.appendChild(dom.createTextNode("\n\t\t"));
                property.appendChild(dom.createTextNode(parameter_name));
            }
            curr_iter.appendChild(property);
            
            property = dom.createElement("Parameter_guesses");
            for (double parameter : data[i].params) {
                property.appendChild(dom.createTextNode("\n\t\t"));
                property.appendChild(dom.createTextNode(parameter + ""));
            }
            curr_iter.appendChild(property);
            property = dom.createElement("Previous_iteration_parameter_standard_error");
            for(int j = 0; j < data[i].params.length; j++){
                property.appendChild(dom.createTextNode("\n\t\t"));
                property.appendChild(dom.createTextNode("Not yet determined."));
            }
            property = dom.createElement("Jacobian_diagonal");
            for (double jacdiag : data[i].jacobian_diagonal) {
                property.appendChild(dom.createTextNode("\n\t\t"));
                property.appendChild(dom.createTextNode(jacdiag + ""));
            }
            curr_iter.appendChild(property);
            
            property = dom.createElement("Lambda");
            property.appendChild(dom.createTextNode(data[i].lambda + ""));
            curr_iter.appendChild(property);
            
            property = dom.createElement("Sum_of_square_residuals");
            property.appendChild(dom.createTextNode(data[i].sum_square_residuals + ""));
            curr_iter.appendChild(property);
            
            property = dom.createElement("Data_filepath");
            property.appendChild(dom.createTextNode(data[i].data_file_path));
            curr_iter.appendChild(property);
            
            root.appendChild(curr_iter);
        }
        dom.appendChild(root);
        
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(filepath)));
    }
}
