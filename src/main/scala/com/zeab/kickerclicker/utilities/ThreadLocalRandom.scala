package com.zeab.kickerclicker.utilities

//Imports
import java.time.{Clock, Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.{ThreadLocalRandom => JavaThreadLocalRandom}

import scala.math.BigDecimal.RoundingMode
import scala.math.BigDecimal.RoundingMode.RoundingMode

//TODO Update the double formatted to actually return number of decimal places correctly for trailing zeros

/**
 * A collection of useful functions to generate random values for all types
 * Utilizes Java's ThreadLocalRandom rather than the Regular Random
 *
 * Per my current understanding while the regular Java random functions is thread safe
 * If used concurrently the regular random could contention and thus performance loss
 * Using ThreadLocalRandom's implementation of random solves that problem
 *
 * @author Kevin Kosnik-Downs (Zeab)
 * @since 2.12
 */
trait ThreadLocalRandom {

  /** Sets the random seed for every ThreadLocalRandom call
   *
   * @param seed The value to use to seed the ThreadLocalRandom calls
   * @return Unit
   */
  def setRandomSeed(seed: Long): Unit = JavaThreadLocalRandom.current().setSeed(seed)

  /** Returns the next generated pseudorandom Double between user defined max and user defined min from the current thread local random instance with specific amount of decimal places.
   *
   * @param max           The greatest Double the return can possibly be (inclusive)
   * @param min           The least Double the return can possibly be (inclusive)
   * @param decimalPlaces The amount of decimal places in the returned value
   * @return A pseudorandom Double
   */
  def getRandomDoubleFormatted(max: Double = Double.MaxValue, min: Double = 1.0, decimalPlaces: Int = 2, isZerosStripped: Boolean = true, roundingMode: RoundingMode = RoundingMode.FLOOR): String = {
    val randomValue: BigDecimal = BigDecimal(getRandomDouble(max, min)).setScale(decimalPlaces, roundingMode)
    if (isZerosStripped) {
      val strippedValue: String = randomValue.bigDecimal.stripTrailingZeros().toPlainString
      if (strippedValue.contains('.')) strippedValue
      else s"$strippedValue.0"
    }
    else randomValue.toString()
  }

  /** Returns the next generated pseudorandom between Double.MaxValue and 1.0 from the current thread local random instance.
   *
   * @param max The greatest Double the return can possibly be (inclusive)
   * @param min The least Double the return can possibly be (inclusive)
   * @return A pseudorandom Double
   * @note Within the java implementation of ThreadLocalRandom
   *       The origin (min) is inclusive
   *       The bound (max) is exclusive
   *       Have to do a little math on the decimal point to determine how much you need to add in order to make the max value inclusive
   *       We don't need to add 1 to the double that are valid Int's because a max of 1 min of 0 would result in a possible value of 1.1
   */
  def getRandomDouble(max: Double = Double.MaxValue, min: Double = 1.0): Double = max match {
    case Double.MaxValue => JavaThreadLocalRandom.current.nextDouble(min, max)
    case max: Double =>
      if (max.isValidInt) JavaThreadLocalRandom.current.nextDouble(min, max)
      else {
        val decimalPlaces: Int = max.toString.split('.').lastOption.getOrElse("").length
        val maxInclusive: Double = ("0." + List.fill(decimalPlaces)(0).mkString("", "", "1")).toDouble + max
        JavaThreadLocalRandom.current.nextDouble(min, maxInclusive)
      }
  }

  /** Returns the next generated pseudorandom between Double.MaxValue and 1.0 from the current thread local random instance.
   *
   * @param max The greatest Big Decimal the return can possibly be (inclusive)
   * @param min The least Big Decimal the return can possibly be (inclusive)
   * @return A pseudorandom BigDecimal
   * @note Within the java implementation of ThreadLocalRandom
   *       The origin (min) is inclusive
   *       The bound (max) is exclusive
   *       Have to do a little math on the decimal point to determine how much you need to add in order to make the max value inclusive
   *       We don't need to add 1 to the double that are valid Int's because a max of 1 min of 0 would result in a possible value of 1.1
   */
  def getRandomBigDecimal(max: BigDecimal = BigDecimal(Double.MaxValue) , min: BigDecimal = 1.0, decimalPlaces: Int = 2): BigDecimal = {
    val randomBigDecimal: BigDecimal = BigDecimal(getRandomDouble(max.toDouble, min.toDouble)).setScale(decimalPlaces)
    if (randomBigDecimal <= min & randomBigDecimal <= max) randomBigDecimal
    else getRandomBigDecimal(max, min, decimalPlaces)
  }

  /** Returns the next generated pseudorandom between Float.MaxValue and Float.MinValue from the current thread local random instance.
   *
   * @param max The greatest Float the return can possibly be (inclusive)
   * @param min The least Float the return can possibly be (inclusive)
   * @return A pseudorandom Float
   * @note Within the java implementation of ThreadLocalRandom
   *       The origin (min) is inclusive
   *       The bound (max) is exclusive
   *       Have to do a little math on the decimal point to determine how much you need to add in order to make the max value inclusive
   *       We don't need to add 1 to the double that are valid Int's because a max of 1 min of 0 would result in a possible value of 1.1
   */
  def getRandomFloat(max: Float = Float.MaxValue, min: Float = 1.0F): Float =
    max match {
      case Float.MaxValue => getRandomDouble(max, min).toFloat
      case max: Float =>
        if (max.isValidInt) getRandomDouble(max, min).toFloat
        else {
          val decimalPlaces: Int = max.toString.split('.').lastOption.getOrElse("").length
          val maxInclusive: Double = ("0." + List.fill(decimalPlaces)(0).mkString("", "", "1")).toDouble + max
          getRandomDouble(maxInclusive, min).toFloat
        }
    }

  /** Returns the next generated pseudorandom Date and Time
   *
   * @param pastOffsetInSeconds   The most distant a datetime should in seconds to be considered for the time range
   * @param futureOffsetInSeconds The least distant a datetime should in seconds to be considered for the time range
   * @param mediumDateTime        The middle of the datetime range
   * @return A pseudorandom ZonedDateTime
   */
  def getRandomZonedDateTimeBetweenOffsets(
                                            pastOffsetInSeconds: Long,
                                            futureOffsetInSeconds: Long,
                                            mediumDateTime: ZonedDateTime
                                          ): ZonedDateTime = {
    val alphaDateTime: ZonedDateTime = mediumDateTime.minusSeconds(pastOffsetInSeconds)
    val omegaDateTime: ZonedDateTime = mediumDateTime.plusSeconds(futureOffsetInSeconds)
    getRandomZonedDateTime(alphaDateTime, omegaDateTime, mediumDateTime.getZone.toString)
  }

  /** Returns the next generated pseudorandom Date and Time
   *
   * @param alphaDateTime The most distant a timestamp should be for the time range
   * @param omegaDateTime The least distant a timestamp should be for the time range
   * @param zoneId        The timezone
   * @return A pseudorandom ZonedDateTime
   */
  def getRandomZonedDateTime(alphaDateTime: ZonedDateTime, omegaDateTime: ZonedDateTime, zoneId: String = Clock.systemDefaultZone().getZone.toString): ZonedDateTime = {
    val min: Long = alphaDateTime.toInstant.toEpochMilli
    val max: Long = omegaDateTime.toInstant.toEpochMilli
    val randomDateTimeEpoch: Long = getRandomLong(max, min)
    Instant.ofEpochMilli(randomDateTimeEpoch).atZone(ZoneId.of(zoneId))
  }

  /** Returns the next generated pseudorandom between Long.MaxValue and Long.MinValue from the current thread local random instance.
   *
   * @param max The greatest Long the return can possibly be (inclusive)
   * @param min The least Long the return can possibly be (inclusive)
   * @return A pseudorandom Long
   * @note Within the java implementation of ThreadLocalRandom
   *       The origin (min) is inclusive
   *       The bound (max) is exclusive
   *       Which is why its necessary to sometimes add 1 behind the scenes to the max value so this becomes inclusive
   */
  def getRandomLong(max: Long = Long.MaxValue, min: Long = 1L): Long = max match {
    case Int.MaxValue => JavaThreadLocalRandom.current.nextLong(min, max)
    case _ => JavaThreadLocalRandom.current.nextLong(min, max + 1L)
  }

  /** Returns a string of pseudorandom numbers and lower/upper case letters from the current thread local random instance.
   *
   * @param maxLength The greatest number of char the random string the return can possibly be (inclusive)
   * @param minLength The least number of char the random string will be; defaults to max value
   * @return A pseudorandom Letters and Numbers string
   */
  def getRandomAlphaNumeric(maxLength: Int, minLength: Int = 0): String = {
    val chars: Seq[Char] = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    getRandomString(maxLength, minLength, chars)
  }

  /** Returns a string of pseudorandom lower and upper case letters from the current thread local random instance.
   *
   * @param maxLength The greatest number of char the random string the return can possibly be (inclusive)
   * @param minLength The least number of char the random string will be defaults to max value
   * @return A pseudorandom Letters only string
   */
  def getRandomAlpha(maxLength: Int, minLength: Int = 0): String = {
    val chars: Seq[Char] = ('a' to 'z') ++ ('A' to 'Z')
    getRandomString(maxLength, minLength, chars)
  }

  /** Returns a string of pseudorandom user defined input from the current thread local random instance.
   *
   * This is the work horse of the entire Random String set of functions as they are all just hard coded user defined char sets
   * It is also where the random length of the string is determined for all of them
   *
   * @param chars     The seq of character's you wish to choose from to form this string
   * @param maxLength The greatest number of char the random string the return can possibly be
   * @param minLength The least number of char the random string will be; defaults to max value
   * @return A pseudorandom string from user defined values
   */
  def getRandomString(maxLength: Int, minLength: Int, chars: Seq[Char]): String = {
    //Figure out if its a random length or a static length
    val length: Int =
      if (minLength == 0) maxLength
      else getRandomInt(maxLength, minLength)
    List.fill(length)(getRandomItemFromCollection(chars)).mkString
  }

  //Grab a random item from a seq
  def getRandomItemFromCollection[T](collection: Seq[T]): T =
    if (collection.isEmpty) throw new Exception("No items provided to randomly pick from")
    else collection(getRandomInt(collection.size - 1, 0))

  /** Returns the next generated pseudorandom between Int.MaxValue and Int.MinValue from the current thread local random instance.
   *
   * @param max The greatest Int the return can possibly be (inclusive)
   * @param min The least Int the return can possibly be (inclusive)
   * @return A pseudorandom Int
   * @note Within the java implementation of ThreadLocalRandom
   *       The origin (min) is inclusive
   *       The bound (max) is exclusive
   *       Which is why its necessary to sometimes add 1 behind the scenes to the max value so this becomes inclusive
   */
  def getRandomInt(max: Int = Int.MaxValue, min: Int = 1): Int = max match {
    case Int.MaxValue => JavaThreadLocalRandom.current.nextInt(min, max)
    case _ => JavaThreadLocalRandom.current.nextInt(min, max + 1)
  }

  /** Returns a string of pseudorandom numbers from the current thread local random instance.
   *
   * @param maxLength The greatest number of char the random string the return can possibly be
   * @param minLength The least number of char the random string will be; defaults to max value
   * @return A pseudorandom Letters and Numbers string
   */
  def getRandomNumeric(maxLength: Int, minLength: Int = 0): String = {
    val chars: Seq[Char] = '0' to '9'
    getRandomString(maxLength, minLength, chars)
  }

  /** Returns a boolean that is pseudorandom from the current thread local random instance.
   *
   * @return A pseudorandom boolean
   */
  def getRandomBoolean: Boolean = JavaThreadLocalRandom.current().nextBoolean()

}

object ThreadLocalRandom extends ThreadLocalRandom