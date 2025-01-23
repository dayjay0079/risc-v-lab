package lib.peripherals

import chisel3._
import chisel3.util._
import lib.Bus

class MemoryMappedSevenSegment extends Module {
  val io = IO(new Bundle {

    val port = Bus.RespondPort()

    val display = Output(UInt(8.W))
    val anode = Output(UInt(4.W))
  })

  def decodeSevSeg(value: UInt): UInt = {
    val sevSeg = WireDefault(0.U(8.W))
    switch(value) {
      is(0.U) {sevSeg := "b00111111".U}  // 0
      is(1.U) {sevSeg := "b00000110".U}  // 1
      is(2.U) {sevSeg := "b01011011".U}  // 2
      is(3.U) {sevSeg := "b01001111".U}  // 3
      is(4.U) {sevSeg := "b01100110".U}  // 4
      is(5.U) {sevSeg := "b01101101".U}  // 5
      is(6.U) {sevSeg := "b01111101".U}  // 6
      is(7.U) {sevSeg := "b00000111".U}  // 7
      is(8.U) {sevSeg := "b01111111".U}  // 8
      is(9.U) {sevSeg := "b01101111".U}  // 9
      is(10.U) {sevSeg := "b01110111".U} // A
      is(11.U) {sevSeg := "b01111100".U} // b
      is(12.U) {sevSeg := "b00111001".U} // C
      is(13.U) {sevSeg := "b01011110".U} // d
      is(14.U) {sevSeg := "b01111001".U} // E
      is(15.U) {sevSeg := "b01110001".U} // F
    }
    sevSeg
  }

  val value_reg = RegInit(0.U(16.W))
  val anode_reg = RegInit(0.U(21.W))
  val display = RegInit(0.U(8.W))
  val anode = RegInit(0.U(4.W))

  when(io.port.write) {
    value_reg := io.port.wrData
  }
  io.port.rdData := display
  anode_reg := anode_reg + 1.U

  switch(anode_reg) {
    is((0*400000).U) {
      display := decodeSevSeg(value_reg(3, 0))
      anode := "b0001".U
    }
    is((1*400000).U) {
      display := decodeSevSeg(value_reg(7, 4))
      anode := "b0010".U
    }
    is((2*400000).U) {
      display := decodeSevSeg(value_reg(11, 8))
      anode := "b0100".U
    }
    is((3*400000).U) {
      display := decodeSevSeg(value_reg(15, 12))
      anode := "b1000".U
    }
    is((4*400000).U) {
      anode_reg := 0.U
    }
  }

  io.display := (~display).asUInt
  io.anode := (~anode).asUInt
}
