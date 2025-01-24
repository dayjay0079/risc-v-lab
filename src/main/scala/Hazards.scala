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
    val flush_hazards = Input(Bool())

    // Outputs
    val ctrl_nop = Output(new ControlBus)
    val EX_control = Output(UInt(4.W))
    val stall = Output(Bool())
  })
  // NOP ctrl
  io.ctrl_nop.pc := 0.U
  io.ctrl_nop.pc_prediction := 0.U
  io.ctrl_nop.opcode := "x13".U
  io.ctrl_nop.funct3 := 0.U
  io.ctrl_nop.funct7 := 0.U
  io.ctrl_nop.inst_type := 1.U
  io.ctrl_nop.store_type := 0.U
  io.ctrl_nop.load_type := 0.U
  io.ctrl_nop.mem_to_reg := 0.U
  io.ctrl_nop.branch_taken := false.B
  io.ctrl_nop.write_enable_reg := false.B

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

  val hz_flush = Wire(new HazardInfo)
  hz_flush.rd := 0.U
  hz_flush.opcode := 0.U

  val hz_EX = RegNext(Mux(io.flush_hazards, hz_flush, hz_ID)) // Saving EX info
  val hz_MEM = RegNext(Mux(io.flush_hazards, hz_flush, hz_EX)) // MEM info
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
    hz_ID.opcode := 0.U
    hz_ID.rd := 0.U
  }

  // placeholder booleans
  val hz_EX_bool = (hz_EX.opcode === R_Type) | (hz_EX.opcode === I_Type_1) | (hz_EX.opcode === I_Type_2) | (hz_EX.opcode === I_Type_3) | (hz_EX.opcode === U_Type_1) | (hz_EX.opcode === U_Type_2)
  val hz_MEM_bool = (hz_MEM.opcode === R_Type) | (hz_MEM.opcode === I_Type_1) | (hz_MEM.opcode === I_Type_2) | (hz_MEM.opcode === I_Type_3) | (hz_MEM.opcode === U_Type_1) | (hz_MEM.opcode === U_Type_2)
  val hz_WB_bool = (hz_WB.opcode === R_Type) | (hz_WB.opcode === I_Type_1) | (hz_WB.opcode === I_Type_2) | (hz_WB.opcode === I_Type_3) | (hz_WB.opcode === U_Type_1) | (hz_WB.opcode === U_Type_2)

  // Default control signal
  io.EX_control := 0.U

  // if statements for 4 bit control signal for EX stage
  when(hz_EX_bool & ((hz_EX.rd === io.rs1) | (hz_EX.rd === io.rs2))) {
    when((hz_EX.rd === io.rs1) && (hz_EX.rd === io.rs2)) {
      io.EX_control := 7.U // For EX_rd / EX_rd
    } .elsewhen(hz_EX.rd === io.rs1) {
      when(hz_MEM_bool & hz_MEM.rd === io.rs2) {
        io.EX_control := 10.U // For EX_rd / MEM_rd
      } .elsewhen(hz_WB_bool & hz_WB.rd === io.rs2) {
        io.EX_control := 11.U // For EX_rd / WB_rd
      } .otherwise {
        io.EX_control := 4.U  // For EX_rd / rs2
      }
    } .elsewhen(hz_EX.rd === io.rs2) {
      when(hz_MEM_bool & hz_MEM.rd === io.rs1) {
        io.EX_control := 12.U // For MEM_rd / EX_rd
      } .elsewhen(hz_WB_bool & hz_WB.rd === io.rs1) {
        io.EX_control := 13.U // For WB_rd / EX_rd
      } .otherwise {
        io.EX_control := 1.U // For rs1 / EX_rd
      }
    }
  } .elsewhen (hz_MEM_bool & ((hz_MEM.rd === io.rs1) | (hz_MEM.rd === io.rs2))) {
    when((hz_MEM.rd === io.rs1) & (hz_MEM.rd === io.rs2)) {
      io.EX_control := 8.U // For MEM_rd / MEM_rd
    }.elsewhen(hz_MEM.rd === io.rs1) {
      when(hz_WB_bool & hz_WB.rd === io.rs2) {
        io.EX_control := 15.U // For MEM_rd / WB_rd
      }.otherwise {
        io.EX_control := 5.U // For MEM_rd / rs2
      }
    }.elsewhen(hz_MEM.rd === io.rs2) {
      when(hz_WB_bool & hz_WB.rd === io.rs1) {
        io.EX_control := 14.U // For WB_rd / MEM_rd
      }.otherwise {
        io.EX_control := 2.U // For rs1 / MEM_rd
      }
    }
  }.elsewhen (hz_WB_bool & ((hz_WB.rd === io.rs1) | (hz_WB.rd === io.rs2))) {
    when((hz_WB.rd === io.rs1) & (hz_WB.rd === io.rs2)) {
      io.EX_control := 9.U // For WB_rd / WB_rd
    }.elsewhen(hz_WB.rd === io.rs1) {
      io.EX_control := 6.U // For WB_rd / rs2
    }.elsewhen(hz_WB.rd === io.rs2) {
      io.EX_control := 3.U // For rs1 / WB_rd
    }
  }
}
