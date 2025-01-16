import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode.decoder

class HazardInfo extends Bundle {
  val rd = UInt(5.W) // Destination register ID
  val opcode = UInt(7.W) // opcode for instruction types
  }
class Hazards extends Module{
  val io = IO(new Bundle{
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val input = Input(new PipelineValuesEX)

    // Outputs
    val EX_control = Output(UInt(4.W))
    val stall_IF = Wire(Bool())
    val stall_ID = Wire(Bool())
  })

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

  // Stall booleans:

  val stall_counter = RegInit(0.U(2.W)) // 2-bit counter for double stall
  val stall_active = Wire(Bool())

  stall_active := stall_counter =/= 0.U
  io.stall_IF := false.B
  io.stall_ID := false.B

  // Registers tracking instructions for other stages
  val hz_ID = Wire(new HazardInfo)
  hz_ID.rd := io.rd
  hz_ID.opcode := io.input.ctrl.opcode

  val hz_EX = RegNext(hz_ID) // Saving EX info
  val hz_MEM = RegNext(hz_EX) // MEM info
  val hz_WB = RegNext(hz_MEM) // ...


  // Stall ID and IF once for load-use hazard
  when(hz_EX.opcode === I_Type_2 && (hz_EX.rd === io.rs1 || hz_EX.rd === io.rs2)) {
    io.stall_ID := true.B
    stall_counter += 1
  }

  // Stall IF twice for Branching (not branch-prediction compatible)
  when(hz_ID.opcode === B_Type) {
    stall_counter += 1
  }

  // Always stall IF if counter active
  io.stall_IF := stall_counter



  // 16 combinations of rs1/rs2 overwrites (0 = no change)
  io.EX_control := 0.U

  // if statements for 4 bit control signal for EX stage
  when (hz_WB.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
    when((hz_WB.rd === io.rs1) & (hz_WB.rd === io.rs2)) {
      io.EX_control := 9.U // For WB_rd / WB_rd
    }
    when(hz_MEM.rd === io.rs1) {
      io.EX_control := 6.U // For WB_rd / rs2
    }

    when(hz_MEM.rd === io.rs2) {
      io.EX_control := 3.U // For rs1 / WB_rd
    }
  }

  when (hz_MEM.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
    when((hz_MEM.rd === io.rs1) & (hz_MEM.rd === io.rs2)) {
      io.EX_control := 8.U // For MEM_rd / MEM_rd
    }
    when(hz_MEM.rd === io.rs1) {
      when(hz_WB.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_WB.rd === io.rs2) {
          io.EX_control := 14.U // For MEM_rd / WB_rd
        }
      }
      io.EX_control := 5.U // For MEM_rd / rs2
    }

    when(hz_MEM.rd === io.rs2) {
      when(hz_WB.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_WB.rd === io.rs1) {
          io.EX_control := 15.U // For WB_rd / MEM_rd
        }
      }
      io.EX_control := 2.U // For rs1 / MEM_rd
    }
  }

  when (hz_EX.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
    when((hz_EX.rd === io.rs1) & (hz_EX.rd === io.rs2)) {
      io.EX_control := 7.U // For EX_rd / EX_rd
    }
    when(hz_EX.rd === io.rs1) {
      when(hz_MEM.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_MEM.rd === io.rs2) {
          io.EX_control := 10.U // For EX_rd / MEM_rd
        }
      }
      when(hz_WB.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_WB.rd === io.rs2) {
          io.EX_control := 11.U // For EX_rd / WB_rd
        }
      }
      io.EX_control := 4.U // For EX_rd / rs2
    }

    when(hz_EX.rd === io.rs2) {
      when(hz_MEM.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_MEM.rd === io.rs1) {
          io.EX_control := 12.U // For MEM_rd / EX_rd
        }
      }
      when(hz_WB.opcode === (R_Type | I_Type_1 | I_Type_2 | I_Type_3 | U_Type_1 | U_Type_2)) {
        when(hz_WB.rd === io.rs1) {
          io.EX_control := 13.U // For WB_rd / EX_rd
        }
      }
      io.EX_control := 1.U // For rs1 / EX_rd
    }
  }

}
