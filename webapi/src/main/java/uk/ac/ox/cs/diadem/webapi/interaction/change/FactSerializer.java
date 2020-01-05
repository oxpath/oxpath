package uk.ac.ox.cs.diadem.webapi.interaction.change;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface FactSerializer {

  /**
   * Builds a single fact given relation name and terms, and writes it in the provided writer
   * 
   * @param destination
   * @param relationName
   * @param params
   */
  void outputFact(String relationName, Pair<String, String>... params);

  /**
   * Builds a single fact given relation name and terms, and writes it in the provided writer
   * 
   * @param destination
   * @param relationName
   * @param params
   */
  void outputFact(String relationName, List<Pair<String, String>> params);

}