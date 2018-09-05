package com.avlesi.advertisement

import com.avlesi.advertisement.model.{AdClient, AdRule, ClientDetails, Rule}
import com.avlesi.advertisement.util.ConstraintChecker
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConstraintCheckerTest extends FunSuite {

  test("check against rule") {

    val rule = Rule(Option(List("male", "female")),
      Option(List(15, 50)),
      Option(List("France", "Germany")),
      Option(List("Paris", "Berlin"))
    )
    val adRule = AdRule("0", Option("dummyName"), Option("anounce0"), Option(rule))

    val adGoodClient = AdClient("client0", Option(ClientDetails(Option("Mika"), Option("female"), Option(19),
      Option("France"), Option("Paris"))))

    val adClientWithBadAge = AdClient("client1", Option(ClientDetails(Option("John"), Option("male"), Option(10),
      Option("France"), Option("Paris"))))

    val adClientWithBadCountry = AdClient("client2", Option(ClientDetails(Option("Darwin"), Option("male"), Option(20),
      Option("USA"), Option("Paris"))))

    val adClientWithBadCity = AdClient("client2", Option(ClientDetails(Option("Darwin"), Option("male"), Option(20),
      Option("France"), Option("Nantes"))))

    val adClientWithoutAge = AdClient("client1", Option(ClientDetails(Option("John"), Option("male"), None,
      Option("France"), Option("Paris"))))

    assert(ConstraintChecker.isRuleSatisfy(adGoodClient.clientDetails, adRule.rule))
    assert(!ConstraintChecker.isRuleSatisfy(adClientWithBadAge.clientDetails, adRule.rule))
    assert(!ConstraintChecker.isRuleSatisfy(adClientWithBadCountry.clientDetails, adRule.rule))
    assert(!ConstraintChecker.isRuleSatisfy(adClientWithBadCity.clientDetails, adRule.rule))
    assert(!ConstraintChecker.isRuleSatisfy(adClientWithoutAge.clientDetails, adRule.rule))
  }

  test("check against rule without age constraint") {
    val rule = Rule(Option(List("male", "female")),
      None,
      Option(List("France", "Germany")),
      Option(List("Paris", "Berlin"))
    )
    val adRule = AdRule("0", Option("dummyName"), Option("anounce0"), Option(rule))

    val adClient = AdClient("client0", Option(ClientDetails(Option("Mika"), Option("female"), Option(19),
      Option("France"), Option("Paris"))))

    val adClient1 = AdClient("client1", Option(ClientDetails(Option("Gennifer"), Option("female"), Option(40),
      Option("France"), Option("Paris"))))

    val adClient2 = AdClient("client2", Option(ClientDetails(Option("Elena"), Option("female"), None,
      Option("France"), Option("Paris"))))

    assert(ConstraintChecker.isRuleSatisfy(adClient.clientDetails, adRule.rule))
    assert(ConstraintChecker.isRuleSatisfy(adClient1.clientDetails, adRule.rule))
    assert(ConstraintChecker.isRuleSatisfy(adClient2.clientDetails, adRule.rule))
  }
}
