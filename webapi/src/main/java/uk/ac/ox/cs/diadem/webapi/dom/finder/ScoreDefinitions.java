package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.ArrayList;
import java.util.List;

/**
 * The ScoreDefinitions class defines a list of AttributeScores that specifies the robustness of element/attribute
 * combinations. The class serves as central access point for all scoring metrics within the robustness framework.
 * <p>
 * 
 * For instance, the majority vote method used by AddressedNode or the relative position score used by
 * XPathFinderByPosition, as explained below, are both located in the ScoreDefinitions class. The class contains all
 * rules, such as the maximum number of steps in XPathFinderByAnchor, and scoring parameters, such as the score
 * implications of each step along a specified axis.
 * <p>
 * 
 * Consequently, ScoreDefinitions is the only class needed to be changed or replaced in order to evaluate different
 * scoring metrics.
 * 
 * @author JochenK
 * @author Giovanni Grasso
 * 
 */
class ScoreDefinitions {

  /** Score serves as benchmark, normalized to 100. */
  public static final int CANONICAL_PATH_SCORE = 100;

  // score based on attribute, independent of element
  /** High score, because unique identifier. */
  public static final int ID_ATTRIBUTE_SCORE = 500;

  /** High score, because unique identifier, similar to id. */
  public static final int CLASS_ATTRIBUTE_SCORE = 450;

  /** Lower score, because lower selectivity. */
  public static final int UNDEFINED_ATTRIBUTE_SCORE = 200;

  /** All attribute values longer than this constant are discarded. */
  public static final int MAX_VALUE_LENGTH = 50;

  /** All attribute values longer than this constant are discarded. */
  public static final int MAX_NUMBER_FOR_ATTRIBUTE_TOKENS = 2;

  /** Higher score, if attribute value is uniquely contained. */
  public static final int ATTRIBUTE_VALUE_CONTAINS = 20;

  /** List of scores based on element/attribute combinations. */
  public static final List<AttributeScore> attributeScores = new ArrayList<AttributeScore>();
  static {
    // high score, because unique identifier, similar to id
    attributeScores.add(new AttributeScore("INPUT", "name", 500));
    // lower score, because language-dependent
    attributeScores.add(new AttributeScore("INPUT", "value", 400));
    // lower score, because lower selectivity
    attributeScores.add(new AttributeScore("INPUT", "type", 300));

    // high score, because unique identifier, similar to id
    attributeScores.add(new AttributeScore("SELECT", "name", 450));
    attributeScores.add(new AttributeScore("LABEL", "for", 500));

    // lower score, deprecated in XHTML, replaced by id
    attributeScores.add(new AttributeScore("A", "name", 350));
    // low score, because language-dependent
    attributeScores.add(new AttributeScore("A", "title", 100));
    // low score, because result-dependent
    attributeScores.add(new AttributeScore("A", "href", 100));

    // low score, because language-dependent
    attributeScores.add(new AttributeScore("IMG", "alt", 100));
    // low score, because result-dependent
    attributeScores.add(new AttributeScore("IMG", "src", 100));
  }

  // scores based on the unique position
  /** Higher score, because range restricted to siblings. */
  public static final int POSITION_PARENT_SCORE = 275;

  /** Lower score, because range spanning whole tree. */
  public static final int POSITION_ROOT_SCORE = 225;

  /** Score difference, used when converting expressions. */
  public static final int POSITION_ROOT_REDUCTION = POSITION_PARENT_SCORE - POSITION_ROOT_SCORE;

  /** Positional predicates above this limit are discarded. */
  public static final int MAX_POSITION_COUNT = 40;

  private static final int CONCAT_CHILD_PENALTY = 30;

  private static final int CONCAT_DESCENDENT_OR_SELF_PENALTY = 60;

  /**
   * Adjust robustness score based on position and count.
   * 
   * @param position
   *          the position
   * @param count
   *          the count
   * @param score
   *          the initial robustness score
   * @return the adjusted robustness score
   */
  public static float getRelativePositionScore(final float position, final float count, final float score) {
    // no score changes for uniquely identifiable element
    if ((position <= 1) && (count <= 1)) // TODO equality or what? Can count == 0 hold?
      return score;
    // lower score for higher count and position
    return (float) (Math.sqrt(1 - (position / (count + 1))) * score);
  }

  /**
   * Adjust robustness score based on position and count. This is a wrapper method with integer signature.
   * 
   * @param position
   *          the position
   * @param count
   *          the count
   * @param score
   *          the initial robustness score
   * @return the adjusted robustness score
   */
  public static int getRelativePositionScore(final int position, final int count, final int score) {
    return Math.round(getRelativePositionScore((float) position, (float) count, (float) score));
  }

  /**
   * Computes the score of an expression built by concatenating them with '/', given the score of the first and second
   * expression.
   * 
   * @param firstScore
   * @param secondScore
   * @return
   */
  public static int getConcatChildScore(final int firstScore, final int secondScore) {
    final int minScore = Math.min(firstScore, secondScore);
    return Math.max(0, minScore - CONCAT_CHILD_PENALTY);
  }

  /**
   * Computes the score of an expression built by concatenating them with '//', given the score of the first and second
   * expression.
   * 
   * @param firstScore
   * @param secondScore
   * @return
   */
  public static int getConcatDescendentOrSelfScore(final int firstScore, final int secondScore) {
    final int minScore = Math.min(firstScore, secondScore);
    return Math.max(0, minScore - CONCAT_DESCENDENT_OR_SELF_PENALTY);
  }

  // scores for relative paths to anchor nodes
  /** Maximum number of tree steps when searching anchors. */
  public static final int MAX_AXIS_STEPS = 3;

  /** All relative paths below this score are discarded. */
  // TODO this was 80
  public static final int MIN_RELATIVE_SCORE = 0;

  /** All expressions after the 5 best are discarded. */
  // TODO this was 5 -- we need a lot of paths to find overlaps.
  // but this should probably be controlled in each call instead of using a global setting
  public static final int MAX_RELATIVE_EXPRESSIONS = 15;

  /** Weight of the root-to-anchor path. */
  public static final float PARENT_AXIS_WEIGHT = 0.5f;

  /** Weight of the anchor-to-target path. */
  public static final float RELATIVE_PATH_WEIGHT = 0.2f;

  // scores for generalizing path expressions
  /** The maximum number of allowed location step removals. */
  public static final int MAX_STEP_REMOVALS = 10;

  /** Increase in score for every location step removal. */
  public static final int REMOVE_STEP_BONUS = 12;

  // scores for expressions based on text content
  /** Low base score, because language-dependent. */
  public static final int TEXTCONTENT_BASE_SCORE = 200;

  /** Higher score, if starts-with function. */
  public static final int STARTS_WITH_BONUS = 10;

  /** Lower score, if contains function. */
  public static final int CONTAINS_BONUS = 5;

  /** Text content above this length is discarded. */
  public static final int MAX_TEXTCONTENT_LENGTH = 30;

  /** The longer the text, the lower the score. */
  public static final int TEXTCONTENT_CHAR_REDUCTION = 1;

  /**
   * Consolidate robustness scores from multiple generators as arithmetic mean.
   * 
   * @param scores
   *          the list of robustness scores
   * @return the consolidated robustness score
   */
  public static int calculateWeight(final List<Integer> scores) {
    float sum = 0;
    for (final Integer score : scores)
      sum += score.floatValue();
    return Math.round(sum / scores.size());
  }

}