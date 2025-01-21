import chisel3.{when, _}
import chisel3.util._
import chisel3.util.experimental.decode.decoder
import lib.ControlBus

class HazardInfo extends Bundle {
  val rd = UInt(5.W) // Destination register ID
  val opcode = UInt(7.W) // opcode for instruction types
  }
class Hazards extends Module{
  val io = IO(new Bundle{
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val ctrl = Input(new ControlBus)

    // Outputs
    val ctrl_nop = Output(new ControlBus)
    val EX_control = Output(UInt(4.W))
    val stall = Output(Bool())
  })
  // NOP ctrl
  io.ctrl_nop.pc := DontCare
  io.ctrl_nop.pc_prediction := DontCare
  io.ctrl_nop.opcode := "x13".U
  io.ctrl_nop.funct3 := 0.U
  io.ctrl_nop.funct7 := 0.U
  io.ctrl_nop.inst_type := 1.U
  io.ctrl_nop.store_type := DontCare
  io.ctrl_nop.load_type := DontCare
  io.ctrl_nop.mem_to_reg := DontCare
  io.ctrl_nop.branch_taken := DontCare
  io.ctrl_nop.write_enable_reg := DontCare

  // instruction types:
  val R_Type = "b0110011".U // Arithmetic/Logic
  val I_Type_1 = "b0010011".U // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U // Load
  val I_Type_3 = "b1100111".U // jalr
  val S_Type = "b0100011".U // Store
  val B_Type = "b1100011".U // Branch
  val J_Type = "b1101111".U // jal
  val U_Type_1 = "b0110111".U // lui
  val U_Type_2 = "b0010111".U // auipc


  // Registers tracking instructions for other stages
  val hz_ID = Wire(new HazardInfo)
  hz_ID.rd := io.rd
  hz_ID.opcode := io.ctrl.opcode

  val hz_EX = RegNext(hz_ID) // Saving EX info
  val hz_MEM = RegNext(hz_EX) // MEM info
  val hz_WB = RegNext(hz_MEM) // ...

  // Stall booleans:
  val stall_counter = RegInit(0.U(1.W)) // 2-bit counter for double stall
  io.stall := false.B //maybe change?

  // Decrement with 1 pr cycle and stall_IF to true
  when(stall_counter > 0.U) {
    stall_counter := stall_counter - 1.U
    io.stall := true.B
  }

  // Stall ID and IF once for load-use hazard
  when(hz_EX.opcode === I_Type_2 && (hz_EX.rd === io.rs1 || hz_EX.rd === io.rs2)) {
    io.stall := true.B
  }

  // placeholder booleans
  val hz_EX_bool = (hz_EX.opcode === R_Type) | (hz_EX.opcode === I_Type_1) | (hz_EX.opcode === I_Type_2) | (hz_EX.opcode === I_Type_3) | (hz_EX.opcode === U_Type_1) | (hz_EX.opcode === U_Type_2)
  val hz_MEM_bool = (hz_MEM.opcode === R_Type) | (hz_MEM.opcode === I_Type_1) | (hz_MEM.opcode === I_Type_2) | (hz_MEM.opcode === I_Type_3) | (hz_MEM.opcode === U_Type_1) | (hz_MEM.opcode === U_Type_2)
  val hz_WB_bool = (hz_WB.opcode === R_Type) | (hz_WB.opcode === I_Type_1) | (hz_WB.opcode === I_Type_2) | (hz_WB.opcode === I_Type_3) | (hz_WB.opcode === U_Type_1) | (hz_WB.opcode === U_Type_2)

  // Helper variables for condition checks
  val rs1MatchesEX = hz_EX_bool && (hz_EX.rd === io.rs1)
  val rs2MatchesEX = hz_EX_bool && (hz_EX.rd === io.rs2)
  val rs1MatchesMEM = hz_MEM_bool && (hz_MEM.rd === io.rs1)
  val rs2MatchesMEM = hz_MEM_bool && (hz_MEM.rd === io.rs2)
  val rs1MatchesWB = hz_WB_bool && (hz_WB.rd === io.rs1)
  val rs2MatchesWB = hz_WB_bool && (hz_WB.rd === io.rs2)

  // Default control signal
  io.EX_control := 0.U

  when(rs1MatchesEX || rs2MatchesEX) { // EX stage conditions
    io.EX_control := MuxCase(4.U, Seq(
      (rs1MatchesEX && rs2MatchesEX) -> 7.U,                   // EX_rd / EX_rd
      (rs1MatchesEX && rs2MatchesMEM) -> 10.U,                 // EX_rd / MEM_rd
      (rs1MatchesEX && rs2MatchesWB) -> 11.U,                  // EX_rd / WB_rd
      (rs2MatchesEX && rs1MatchesMEM) -> 12.U,                 // MEM_rd / EX_rd
      (rs2MatchesEX && rs1MatchesWB) -> 13.U,                  // WB_rd / EX_rd
       rs2MatchesEX -> 1.U                                     // rs1 / EX_rd
    ))
  } .elsewhen(rs1MatchesMEM || rs2MatchesMEM) { // MEM stage conditions
      io.EX_control := MuxCase(5.U, Seq(
        (rs1MatchesMEM && rs2MatchesMEM) -> 8.U,                // MEM_rd / MEM_rd
        (rs1MatchesMEM && rs2MatchesWB) -> 14.U,                // MEM_rd / WB_rd
        (rs2MatchesMEM && rs1MatchesWB) -> 15.U,                // WB_rd / MEM_rd
         rs2MatchesMEM -> 2.U                                   // rs1 / MEM_rd
      ))
    } .elsewhen(rs1MatchesWB || rs2MatchesWB) { // WB stage conditions
      io.EX_control := MuxCase(6.U, Seq(
        (rs1MatchesWB && rs2MatchesWB) -> 9.U,                  // WB_rd / WB_rd
         rs2MatchesWB -> 3.U                                    // rs1 / WB_rd
      ))
    }
}
