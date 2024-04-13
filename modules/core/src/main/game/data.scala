package lila.core
package game

import _root_.chess.{ Centis, Clock, Color, Ply }
import _root_.chess.format.pgn.PgnStr
import lila.core.id.{ TourId, SwissId, SimulId }
import lila.core.userId.UserId
import lila.core.rating.data.{ IntRating, IntRatingDiff, RatingProvisional }

case class PlayerUser(id: UserId, rating: IntRating, ratingDiff: Option[IntRatingDiff])
case class UserInfo(id: UserId, rating: IntRating, provisional: RatingProvisional)

case class GameMetadata(
    source: Option[Source],
    pgnImport: Option[PgnImport],
    tournamentId: Option[TourId],
    swissId: Option[SwissId],
    simulId: Option[SimulId],
    analysed: Boolean,
    drawOffers: GameDrawOffers,
    rules: Set[GameRule]
):
  def pgnDate                                  = pgnImport.flatMap(_.date)
  def pgnUser                                  = pgnImport.flatMap(_.user)
  def hasRule(rule: GameRule.type => GameRule) = rules(rule(GameRule))
  def nonEmptyRules                            = rules.nonEmpty.option(rules)

case class GameDrawOffers(white: Set[Ply], black: Set[Ply]):

  def lastBy(color: Color): Option[Ply] = color.fold(white, black).maxOption(intOrdering)

  def add(color: Color, ply: Ply) =
    color.fold(copy(white = white.incl(ply)), copy(black = black.incl(ply)))

  // lichess allows to offer draw on either turn,
  // normalize to pretend it was done on the opponent turn.
  def normalize(color: Color): Set[Ply] = color.fold(white, black).map {
    case ply if ply.turn == color => ply + 1
    case ply                      => ply
  }
  def normalizedPlies: Set[Ply] = normalize(Color.white) ++ normalize(Color.black)

case class PgnImport(
    user: Option[UserId],
    date: Option[String],
    pgn: PgnStr,
    // hashed PGN for DB unicity
    h: Option[Array[Byte]]
)

// times are expressed in seconds
case class CorrespondenceClock(
    increment: Int,
    whiteTime: Float,
    blackTime: Float
):

  import CorrespondenceClock.*

  def daysPerTurn = increment / 60 / 60 / 24

  def remainingTime(c: Color) = c.fold(whiteTime, blackTime)

  def outoftime(c: Color) = remainingTime(c) == 0

  def moretimeable(c: Color) = remainingTime(c) < (increment - hourSeconds)

  def giveTime(c: Color) =
    c.fold(
      copy(whiteTime = whiteTime + daySeconds),
      copy(blackTime = blackTime + daySeconds)
    )

  // in seconds
  def estimateTotalTime = increment * 40 / 2

  def incrementHours = increment / 60 / 60

object CorrespondenceClock:
  val hourSeconds = 60 * 60
  val daySeconds  = 24 * hourSeconds

case class ClockHistory(
    white: Vector[Centis] = Vector.empty,
    black: Vector[Centis] = Vector.empty
):

  def update(color: Color, f: Vector[Centis] => Vector[Centis]): ClockHistory =
    color.fold(copy(white = f(white)), copy(black = f(black)))

  def record(color: Color, clock: Clock): ClockHistory =
    update(color, _ :+ clock.remainingTime(color))

  def reset(color: Color) = update(color, _ => Vector.empty)

  def apply(color: Color): Vector[Centis] = color.fold(white, black)

  def last(color: Color) = apply(color).lastOption

  def size = white.size + black.size

  // first state is of the color that moved first.
  def bothClockStates(firstMoveBy: Color): Vector[Centis] =
    interleave(
      firstMoveBy.fold(white, black),
      firstMoveBy.fold(black, white)
    )

object ClockHistory:
  val someEmpty = Some(ClockHistory())