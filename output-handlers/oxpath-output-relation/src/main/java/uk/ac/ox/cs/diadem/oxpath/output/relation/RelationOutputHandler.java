/*
 * Copyright (c) 2017, OXPath Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the OXPath team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OXPath Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ox.cs.diadem.oxpath.output.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.ACustomOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IWithAccumulativeOutput;
import uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRuntimeException;

/**
 * An output handler for serialising elements of the result tree into a list of tuples.
 * 
 * @author Ruslan Fayzrakhmanov
 * 1 Feb 2017
 */
public class RelationOutputHandler
	extends ACustomOutputHandler
	implements IWithAccumulativeOutput<RelationModel>{
  private static final Logger log = LoggerFactory.getLogger(RelationOutputHandler.class);
  
  public static final String ID_LABEL_PART_DEFAULT = "id";
  
  protected final String idLabelPart;
  protected final boolean includeId;
  protected final boolean includeRecordLabel;
  
  private final IFilter<String> valFilter;
  private final IFilter<List<List<String>>> rowFilter;
	
  	public static interface IFilter<T> {
		public boolean filter(T val);
	}
  	
  	public static IFilter<List<List<String>>> EMPTY_ROW_FILTER = new IFilter<List<List<String>>>() {
		@Override
		public boolean filter(List<List<String>> row) {
			for (List<String> val : row) {
				if (val.size()>0)
					return false;
			}
			return true;
		}
	};
	
  private final Set<List<String>> entityPathSet;
  private final List<List<String>> entityPathList;
  
  /**
   * Instance field holding the output of the nodes received from the output stream
   */
  private List<OXPathExtractionNode> nodes = new Vector<OXPathExtractionNode>();
  private RelationModel relationModel = null;

	  /**
	   * The entityRelPathList represent the list of relative paths for entities to be serialised.
	   * Each path, except the first, is relative to the closest entity-ancestor (without its name).
	   * 
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public RelationOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter) {
		super(allowMultipleValuesPerAttribute);
		this.includeId = includeId;
		this.includeRecordLabel = includeRecordLabel;
		this.idLabelPart = ID_LABEL_PART_DEFAULT;
		assert entityRelPathList.length > 0;
		this.entityPathList = fillEntityPathList(entityRelPathList);
		this.entityPathSet = Sets.newHashSet(this.entityPathList);
		this.valFilter = valFilter;
		this.rowFilter = rowFilter;
	}
  
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public RelationOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter) {
		super(wrapperId, allowMultipleValuesPerAttribute);
		this.includeId = includeId;
		this.includeRecordLabel = includeRecordLabel;
		this.idLabelPart = ID_LABEL_PART_DEFAULT;
		assert entityRelPathList.length > 0;
		this.entityPathList = fillEntityPathList(entityRelPathList);
		this.entityPathSet = newUnmodSet(this.entityPathList);
		this.valFilter = valFilter;
		this.rowFilter = rowFilter;
	}
	
	private List<List<String>> fillEntityPathList(
			String[][] entityRelPathList) {
		List<List<String>> entityPathList = new ArrayList<List<String>>(entityRelPathList.length);
		List<String> recentPathPart = null;
		for (String[] relPathArr: entityRelPathList) {
			List<String> relPath = Lists.newArrayList(relPathArr);
			if (recentPathPart == null)
				recentPathPart = relPath;
			else
				recentPathPart = newAddAllUnmod(recentPathPart, relPath);
			entityPathList.add(Collections.unmodifiableList(recentPathPart));
		}
		return Collections.unmodifiableList(entityPathList);
	}
  
  	@Override
	protected void processNodeImpl(OXPathExtractionNode node) {
  		if (!node.isEndNode()) {
  			assert nodes.size()+1 == node.getId();
  			nodes.add(node);
  	    }
	}

	public RelationModel getAccumulativeOutput() {
		if (relationModel==null) {
			relationModel=convertToRelationModel(nodes, entityPathSet, entityPathList, idLabelPart, includeId, includeRecordLabel);
    		nodes = null;
    	}
    	return relationModel;
	}
	
	private RelationModel convertToRelationModel(List<OXPathExtractionNode> nodes,
			Set<List<String>> entityPathSet,
			List<List<String>> entityPathList
			, String idLabelPart
			, boolean includeId
			, boolean includeRecordLabel) {
		List<Features> mainEntityFeatures = processExtrNodes(nodes, entityPathSet);
		nodes.clear(); nodes = null;
		Object[] o = buildEntityTuples(mainEntityFeatures);
		@SuppressWarnings("unchecked")
		List<List<Features>> entTuples = (List<List<Features>>)o[0];
		@SuppressWarnings("unchecked")
		Map<List<String>, Set<List<String>>> entPathToAttrPathsMap = (Map<List<String>, Set<List<String>>>)o[1];
		RelationModel m = buildRelModel(entTuples, entPathToAttrPathsMap, entityPathList, idLabelPart, includeId, includeRecordLabel);
		return m;
	}
	
	/**
	 * An id which can be used for uniquely identifying different instances of the {@linkplain Features} class.
	 */
	private int freeFeatureId = 0;
	/**
	 * It models necessary features and relations between entities and attributes identified within the OXPath result tree. 
	 * @author Ruslan Fayzrakhmanov
	 */
	private class Features {
		
		public Features(OXPathExtractionNode node) {
			this.node = node;
			this.id = freeFeatureId;
			freeFeatureId++;
		}
		public final int id;
		public final OXPathExtractionNode node;
		public List<String> path = null;
		/**
		 * The path relative either to the root or to the closest entity to be serialised.
		 */
		public List<String> relPath = null;
		/**
		 * It maps relative path of attributes of the same kind (same path and label) to the attributes.
		 */
		public final Map<List<String>, List<Features>> relPathAttrMap = new HashMap<List<String>, List<Features>>();
		public void addAttr(List<String> attrRelPath, Features attr) {
			assert attrRelPath.equals(attr.relPath); 
			List<Features> attrs = relPathAttrMap.get(attrRelPath);
			if (attrs == null) {
				attrs = new ArrayList<Features>();
				relPathAttrMap.put(Collections.unmodifiableList(attrRelPath), attrs);
			}
			attrs.add(attr);
		}
		/**
		 * It sets the correspondence between an entity to be serialised and closest descendants to be serialised.
		 */
		public final List<Features> childEntities = new ArrayList<Features>();
		/**
		 * It sets the correspondence between the entity to be serialised and the closest ancestor to be serialised.
		 */
		public Features inEntity = null;
		public boolean isEntity = false;
	}
	
	/**
	 * It builds a tree-like structure, reflecting the dependencies between entities to be serialised along with their attributes.
	 * 
	 * @param nodes list of extraction nodes obtained from the OXPath parser.
	 * @param entityPathSet List of paths of entities to be serialised. (i+1)-th item is a descendant of the i-th item. 
	 * @return List of the most generic entities (they must have the same label and the path) to be serialised.
	 */
	private List<Features> processExtrNodes(List<OXPathExtractionNode> nodes
			, Set<List<String>> entityPathSet) {
		List<Features> featues = new ArrayList<Features>();
		List<Features> mainEntityFeatures = new ArrayList<Features>();
		
		for (final OXPathExtractionNode node : nodes) {
			boolean isParentRoot = node.getParentId() == 0;
	    	OXPathExtractionNode parentNode = isParentRoot?null:nodes.get(node.getParentId()-1);
	    	assert isParentRoot || parentNode.getId() == node.getParentId();
	    	if (!isParentRoot && parentNode.isAttribute())
	    		throw new OutputHandlerRuntimeException(
						"An attribute node cannot have children", log);
	    	
	    	// initialise the current features class.
	    	Features nodeFeats = new Features(node);
	    	featues.add(node.getId()-1, nodeFeats);
	    	
	    	// get the parent node features
	    	Features parentNodeFeats = null;
	    	if (!isParentRoot)
	    		parentNodeFeats = featues.get(node.getParentId()-1);
	    	
	    	// get the parent entity features.
	    	Features parentEntFeats = null;
	    	if (parentNodeFeats != null) {
	    		if (parentNodeFeats.isEntity)
		    		parentEntFeats = parentNodeFeats;
		    	else if (parentNodeFeats.inEntity != null)
		    		parentEntFeats = featues.get(parentNodeFeats.inEntity.node.getId()-1);
	    	}
	    	
	    	// get the absolute path
	    	List<String> path = newSingletoneList(node.getLabel());
	    	if (parentNodeFeats != null) {
	    		path = newAddAllUnmod(parentNodeFeats.path, path);
	    	}
	    	nodeFeats.path = path;
	    	
	    	// get the relative path, a shortest path, relative either to the root or to the parent entity
	    	List<String> relPath = newSingletoneList(node.getLabel());
	    	if (!isParentRoot) {
	    		if (parentEntFeats == null || parentNode != parentEntFeats.node) {
	    			relPath = newAddAllUnmod(parentNodeFeats.relPath, relPath);
		    	}
	    	}
	    	nodeFeats.relPath = relPath;
	    	
	    	// check if the current extraction node is an entity to be serialised
	    	if (entityPathSet.contains(path)) {
	    		if (node.isAttribute())
	    			throw new OutputHandlerRuntimeException(
							"The attribute node \""+node.getLabel()+"\" cannot be serialised as an entity", log);
	    		nodeFeats.isEntity = true;
	    	}
	    	
	    	// set the relation between the current entity and its parent entity
	    	if (nodeFeats.isEntity) {
	    		if (parentEntFeats == null)
	    			mainEntityFeatures.add(nodeFeats);
    			else {
	    			parentEntFeats.childEntities.add(nodeFeats);
	    			nodeFeats.inEntity = parentEntFeats;
    			}
	    	} else {
	    		if (parentEntFeats != null) {
	    			if (node.isAttribute())
	    				parentEntFeats.addAttr(relPath, nodeFeats);
	    			nodeFeats.inEntity = parentEntFeats;
	    		}
	    	}
    	}
		return mainEntityFeatures;
	}
	
	/**
	 * 
	 * It generates list of tupels--entities to be serialised--and the mapping between entity paths and their relative attribute paths
	 * 
	 * @param mainEntityFeatures is a list of top entities to be serialised.
	 * @return Object array with two items: tuples with entities to be serialised as a relation and the mapping between entity paths and their relative attribute paths
	 */
	private Object[] buildEntityTuples(List<Features> mainEntityFeatures) {
		Map<List<String>, Set<List<String>>> entPathToAttrPathsMap = new HashMap<List<String>, Set<List<String>>>();
		List<List<Features>> entTuples = null;
		for (Features rec : mainEntityFeatures) {
			List<List<Features>> recTuplesTmp = buildEntityTuplesInRec(rec, entPathToAttrPathsMap);
			if (entTuples == null)
				entTuples = recTuplesTmp;
			else
				entTuples.addAll(recTuplesTmp);
		}
		return new Object[]{entTuples, entPathToAttrPathsMap};
	}
	
	/**
	 * Given an entity it builds a list of tuples (with entities to be serialised) based on the subtree rooted at this entity.
	 * This is a recursion which is called from {@linkplain #buildEntityTuples(List)}.
	 * 
	 * @param ent is the current entity
	 * @param entPathToAttrRelPathsMap is the mapping between entity paths and their relative attribute paths 
	 * @return
	 */
	private List<List<Features>> buildEntityTuplesInRec(Features ent, Map<List<String>, Set<List<String>>> entPathToAttrRelPathsMap) {
		List<List<Features>> recTuples = null;
		// call this function recursively in the loop
		for (Features childEnt : ent.childEntities) {
			List<List<Features>> entTuplesTmp = buildEntityTuplesInRec(childEnt, entPathToAttrRelPathsMap);
			if (recTuples == null)
				recTuples = entTuplesTmp;
			else
				recTuples.addAll(entTuplesTmp);
		}
		// if there are no tuples, return only the current entity.
		if (recTuples == null) {
			recTuples = new ArrayList<List<Features>>();
			recTuples.add(Lists.newArrayList(ent));
		} else {
			// if there are elements in the tuple,
			// adjust all tuples to the same size
			// and add the current entity into the beginning of these tuples.
			int maxLen = -1;
			for (List<Features> recTupleTmp : recTuples) {
				if (maxLen < recTupleTmp.size()) {
					maxLen = recTupleTmp.size();
				}
			}
			for (List<Features> recTupleTmp : recTuples) {
				while (recTupleTmp.size() < maxLen) {
					recTupleTmp.add(null);
				}
			}
			for (List<Features> recTupleTmp : recTuples) {
				recTupleTmp.add(0, ent);
			}
		}
		// add relative path of attributes into the mapping
		Set<List<String>> attrPaths = entPathToAttrRelPathsMap.get(ent.path);
		if (attrPaths == null) {
			attrPaths = new HashSet<List<String>>();
			entPathToAttrRelPathsMap.put(ent.path, attrPaths);
		}
		for (List<String> rp: ent.relPathAttrMap.keySet()) {
			attrPaths.add(rp);
		}
		return recTuples;
	}
	
	/**
	 * 
	 * Generate the relation based on the list of tuples of entities obtained from the OXPath extraction tree.
	 * 
	 * @param entTuples is the list of tuples with entities to be serialised.
	 * @param entPathToAttrRelPathsMap is the mapping between entity paths and relative attribute paths.
	 * @param recordPathList List of entity paths to be serialised.
	 * @return relation representing tuples of entities with their attributes to be serialised
	 */
	@SuppressWarnings("unchecked")
	private RelationModel buildRelModel(List<List<Features>> entTuples
			, Map<List<String>, Set<List<String>>> entPathToAttrRelPathsMap
			, List<List<String>> recordPathList
			, String idLabelPart
			, boolean includeId
			, boolean includeRecordLabel) {
		List<Integer> entityIndexes = new ArrayList<Integer>();
		List<List<String>> headers = new ArrayList<List<String>>();
		List<List<String>> headersRelPaths = new ArrayList<List<String>>();
		for (List<String> entPath: recordPathList) {
			if (entPathToAttrRelPathsMap.containsKey(entPath)) {
				entityIndexes.add(headers.size());
				String entLabel = getLastPartOfPath(entPath);
				if (includeId) {
					headers.add(Lists.newArrayList(entLabel, idLabelPart));
				}
				List<List<String>> attrRelPaths = Lists.newArrayList(entPathToAttrRelPathsMap.get(entPath));
				if (includeRecordLabel) {
					for (List<String> attrRelPath: attrRelPaths)
						headers.add(newAddUnmodList(attrRelPath, entLabel, 0));
				} else {
					headers.addAll(attrRelPaths);
				}
				headersRelPaths.add(null);
				headersRelPaths.addAll(attrRelPaths);
			}
		}

		// Generate rows
		RelationModel m = new RelationModel(headers, includeId, entityIndexes);
		List<Features> prevEntityRow = null;
		List<List<String>> prevRow = null;
		// iterate over the list of entity rows
		for (List<Features> entityRow: entTuples) {
			int headerPos = 0;
			List<List<String>> row = new ArrayList<List<String>>(headers.size());
			// iterate over entities within the entity row
			for (int i=0; i<entityRow.size(); i++) {
				Features rec = entityRow.get(i);
				if (includeId) {
					if (prevEntityRow == null) {
						row.add(Lists.newArrayList("0"));
					} else {
						assert rec != null && (headerPos !=0 || prevEntityRow.get(i).id != rec.id); // The first id is the primary key
						if (rec == null || prevEntityRow.get(i).id != rec.id) {
							// increase the current id by 1 if corresponding entity is different
							row.add(Lists.newArrayList(
									String.valueOf(Integer.parseInt(prevRow.get(headerPos).get(0)) + 1) ));
						} else {
							// use the same id if the entity is the same
							row.add(Lists.newArrayList( prevRow.get(headerPos).get(0) ));
						}
					}
				}
				headerPos++;
				// iterate over relative paths of attributes of the current entity
				for (; rec != null && headerPos<headersRelPaths.size() && headersRelPaths.get(headerPos) != null; headerPos++) {
					List<Features> attrs = rec.relPathAttrMap.get(headersRelPaths.get(headerPos));
					if (attrs == null) {
						row.add(Collections.EMPTY_LIST);
					} else {
						List<String> attrVals = new ArrayList<String>(attrs.size());
						for (Features attr: attrs) {
							assert attr.node.isAttribute();
							if (valFilter == null || !valFilter.filter(attr.node.getValue()))
								attrVals.add(attr.node.getValue());
						}
						row.add(attrVals);
					}
					
				}
			}
			if (rowFilter == null || !rowFilter.filter(row)) {
				m.addRow(row);
				prevEntityRow = entityRow;
				prevRow = row;
			}
		}
		
		return m;
	}
	
	private String getLastPartOfPath(List<String> path) {
		assert path.size()>0;
		return path.get(path.size()-1);
	}
	
	private <T> List<T> newAddAllUnmod(List<T> l1, List<T> l2) {
		(l1 = Lists.newArrayList(l1)).addAll(l2);
		return (List<T>)Collections.unmodifiableList(l1);
	}
	
	private <T> Set<T> newUnmodSet(Collection<T> c) {
		return Collections.unmodifiableSet(Sets.newHashSet(c));
	}
	
	private <T> List<T> newAddUnmodList(List<T> l1, T v, int pos) {
		(l1 = Lists.newArrayList(l1)).add(pos, v);
		return (List<T>)Collections.unmodifiableList(l1);
	}
	
	private <T> List<T> newSingletoneList(T v) {
		return Collections.singletonList(v);
	}

}
