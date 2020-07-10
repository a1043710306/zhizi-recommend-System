package ufs

object expriment {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  Util.getIdx("FBA896A057078D1144F57B54DA388D73", "", 6)
                                                  //> res0: Int = 2

  val a = 123                                     //> a  : Int = 123
  s"fdafasf$a"                                    //> res1: String = fdafasf123

  System.currentTimeMillis() / (24 * 3600 * 1000) * (24 * 3600 * 1000)
                                                  //> res2: Long = 1483488000000
                                                  
                                                 // "abc".toLong
                                                  
                                                  (System.currentTimeMillis() / 1000) / (24 * 60 * 60)
                                                  //> res3: Long = 17170
}