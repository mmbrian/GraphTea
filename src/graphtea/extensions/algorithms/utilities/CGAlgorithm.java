/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtea.extensions.algorithms.utilities;

import graphtea.platform.core.BlackBoard;
import graphtea.plugins.algorithmanimator.core.GraphAlgorithm;
import graphtea.plugins.algorithmanimator.extension.AlgorithmExtension;

/**
 *
 * @author Mohsen Mansouryar
 */
public abstract class CGAlgorithm extends GraphAlgorithm implements AlgorithmExtension {

    public CGAlgorithm(BlackBoard blackBoard) {
        super(blackBoard);
    }

    public void doAlgorithm() {}
    
    public String getName() { return ""; }

    public String getDescription() { return ""; }
 
}
