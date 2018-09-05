package com.avlesi.advertisement.util

import com.avlesi.advertisement.model.{ClientDetails, Rule}

object ConstraintChecker {

  val ageConstraintListSize = 2

  def isRuleSatisfy(optionClient: Option[ClientDetails], optionRule: Option[Rule]): Boolean = {

    def isRelevantCity(client: ClientDetails, rule: Rule): Boolean = {

      if (rule.cityConstraint.isDefined) {
        if (client.city.isDefined) {
          val clientCity = client.city.get.toLowerCase
          val cities = rule.cityConstraint.get.map(c => c.toLowerCase())
          cities.contains(clientCity)
        }
        else
          false
      }
      else
        true
    }

    def isRelevantCountry(client: ClientDetails, rule: Rule): Boolean = {
      if(rule.countryConstraint.isDefined) {
        if (client.country.isDefined) {
          val clientCountry = client.country.get.toLowerCase
          val countries = rule.countryConstraint.get.map(c => c.toLowerCase())
          countries.contains(clientCountry)
        }
        else
          false
      }
      else
        true
    }

    def isRelevantSex(client: ClientDetails, rule: Rule): Boolean = {
      if (rule.sexConstraint.isDefined) {
        if (client.sex.isDefined) {
          val clientSex = client.sex.get.toLowerCase
          val sexes = rule.sexConstraint.get.map(s => s.toLowerCase())
          sexes.contains(clientSex)
        }
        else
          false
      }
      else
        true
    }


    def isRelevantAge(client: ClientDetails, rule: Rule): Boolean = {
      if (rule.ageConstraint.isDefined &&
        rule.ageConstraint.get.size == ageConstraintListSize) {
        if (client.age.isDefined) {
          val clientAge = client.age.get
          val startAge = rule.ageConstraint.get(0)
          val endAge = rule.ageConstraint.get(1)
          clientAge >= startAge && clientAge <= endAge
        }
        else
          false
      }
      else
        true
    }


    if (optionRule.isDefined) {
      if (optionClient.isDefined) {
        isRelevantCity(optionClient.get, optionRule.get) &&
          isRelevantCountry(optionClient.get, optionRule.get) &&
          isRelevantSex(optionClient.get, optionRule.get) &&
          isRelevantAge(optionClient.get, optionRule.get)
      }
      else
        false
    }
    else
      true
  }
}
