CREATE (customer1:Customer {customerId:1, contextId: 9, firstName:'Alice'})
CREATE (customer2:Customer {customerId:2, contextId: 9, firstName:'Bob'})
CREATE (customer3:Customer {customerId:3, contextId: 9, firstName:'Charlie'})

CREATE (customer4:Customer {customerId:4, contextId: 10, firstName:'Lucifer'})

CREATE (occ1_1:Occurrence {contextId: 9, end: 166000000})
CREATE (occ1_2:Occurrence {contextId: 9, end: 166000010})
CREATE (occ1_3:Occurrence {contextId: 9, end: 166000020})

CREATE (occ2_1:Occurrence {contextId: 9, end: 166000030})
CREATE (occ2_2:Occurrence {contextId: 9, end: 166000040})

CREATE (occ3_1:Occurrence {contextId: 9, end: 166000050})
CREATE (occ3_2:Occurrence {contextId: 9, end: 166000060})
CREATE (occ3_3:Occurrence {contextId: 9, end: 166000070})
CREATE (occ3_4:Occurrence {contextId: 9, end: 166000080})

CREATE
  (customer1)-[:CLIENT_HAS_OCCURRENCE] ->(occ1_1),
  (customer1)-[:CLIENT_HAS_OCCURRENCE] ->(occ1_2),
  (customer1)-[:CLIENT_HAS_OCCURRENCE] ->(occ1_3),
  (customer2)-[:CLIENT_HAS_OCCURRENCE] ->(occ2_1),
  (customer2)-[:CLIENT_HAS_OCCURRENCE] ->(occ2_2),
  (customer3)-[:CLIENT_HAS_OCCURRENCE] ->(occ3_1),
  (customer3)-[:CLIENT_HAS_OCCURRENCE] ->(occ3_2),
  (customer3)-[:CLIENT_HAS_OCCURRENCE] ->(occ3_3),
  (customer3)-[:CLIENT_HAS_OCCURRENCE] ->(occ3_4)

CREATE
  (occ1_1)-[:THEN]->(occ1_2),
  (occ1_2)-[:THEN]->(occ1_3),
  (occ2_1)-[:THEN]->(occ2_2),
  (occ3_1)-[:THEN]->(occ3_2),
  (occ3_2)-[:THEN]->(occ3_3),
  (occ3_3)-[:THEN]->(occ3_4)

CREATE (action1:TypeInteraction {contextId:9, actionId:1})
CREATE (action2:TypeInteraction {contextId:9, actionId:2})
CREATE (action3:TypeInteraction {contextId:9, actionId:3})

CREATE
  (occ1_1)-[:INTERACTION_OF_TYPE]->(action1),
  (occ1_2)-[:INTERACTION_OF_TYPE]->(action2),
  (occ1_3)-[:INTERACTION_OF_TYPE]->(action1),

  (occ2_1)-[:INTERACTION_OF_TYPE]->(action1),
  (occ2_2)-[:INTERACTION_OF_TYPE]->(action2),

  (occ3_1)-[:INTERACTION_OF_TYPE]->(action1),
  (occ3_2)-[:INTERACTION_OF_TYPE]->(action2),
  (occ3_3)-[:INTERACTION_OF_TYPE]->(action1),
  (occ3_4)-[:INTERACTION_OF_TYPE]->(action3)
